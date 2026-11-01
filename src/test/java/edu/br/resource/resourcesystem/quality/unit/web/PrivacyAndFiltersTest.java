package edu.br.resource.resourcesystem.quality.unit.web;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.config.PrivacyProperties;
import edu.br.resource.resourcesystem.controller.error.*;
import edu.br.resource.resourcesystem.observability.*;
import edu.br.resource.resourcesystem.security.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.*;
import org.springframework.web.servlet.ModelAndView;

@DisplayName("RN-001/RN-017 — respostas seguras e limites de upload")
class PrivacyAndFiltersTest {
    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 399, 400, 401, 403, 404, 405, 409, 413, 429, 500, 502, 503, 599, 600})
    void respond_WithStatus_ReturnsSafeJsonWithoutInternalSecrets(int code) {
        var request = new MockHttpServletRequest("GET", "/api/anything");
        request.setAttribute(
                "jakarta.servlet.error.exception", new IllegalStateException("fictitious-secret"));
        request.setAttribute(RequestLoggingFilter.REQUEST_ID_HEADER, "quality-request");
        var response = new MockHttpServletResponse();
        var result =
                (ResponseEntity<?>)
                        new ErrorResponses(new PrivacyProperties("help@example.test"))
                                .respond(code, request, response);
        assertThat(result.getStatusCode().value()).isEqualTo(code < 400 || code > 599 ? 500 : code);
        var body = (ProblemDetail) result.getBody();
        assertThat(body.getDetail()).doesNotContain("fictitious-secret", "IllegalStateException");
        assertThat(body.getProperties()).containsEntry("requestId", "quality-request");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_DONOR", "ROLE_ONG", "ROLE_ADMINISTRATOR"})
    void respond_WithSignedInBrowser_ProvidesOnlyOwnSafeHome(String role) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "fictitious", "ignored", AuthorityUtils.createAuthorityList(role)));
        var request = new MockHttpServletRequest("GET", "/page");
        request.addHeader("Accept", "text/html");
        var result =
                (ModelAndView)
                        new ErrorResponses(new PrivacyProperties("help@example.test"))
                                .respond(404, request, new MockHttpServletResponse());
        assertThat(result.getModel().get("errorHomePath"))
                .isEqualTo(
                        role.equals("ROLE_DONOR")
                                ? "/donor/dashboard"
                                : role.equals("ROLE_ONG") ? "/institution/dashboard" : "/about");
    }

    @Test
    void respond_WithCsrfFailure_ExplainsExpiredForm() {
        var request = new MockHttpServletRequest("POST", "/page");
        request.setAttribute(ErrorResponses.CSRF_ERROR, true);
        var result =
                (ModelAndView)
                        new ErrorResponses(new PrivacyProperties("help@example.test"))
                                .respond(403, request, new MockHttpServletResponse());
        assertThat(result.getModel()).containsEntry("errorSessionExpired", true);
    }

    @Test
    void failureDescription_WithCycleAndSensitiveMessage_BoundsDepthWithoutMessage() {
        var first = new IllegalStateException("fictitious-secret");
        var second = new RuntimeException("another-secret");
        first.initCause(second);
        second.initCause(first);
        assertThat(FailureDetails.describe(first))
                .contains("IllegalStateException", "RuntimeException")
                .doesNotContain("fictitious-secret", "another-secret");
        assertThat(FailureDetails.describe(null)).isEmpty();
    }

    @Test
    void logging_WithPreviousMdc_RestoresContextAndGeneratesCorrelation() throws Exception {
        MDC.put("previous", "value");
        var request = new MockHttpServletRequest("GET", "/donor/needs?token=fictitious");
        request.setServletPath("/donor/needs");
        var response = new MockHttpServletResponse();
        new RequestLoggingFilter()
                .doFilter(
                        request, response, (r, s) -> assertThat(MDC.get("requestId")).isNotBlank());
        assertThat(response.getHeader("X-Request-ID")).isNotBlank();
        assertThat(MDC.get("previous")).isEqualTo("value");
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void logging_WithRequestFailure_RethrowsAndClearsMdc() {
        var request = new MockHttpServletRequest("GET", "/failing");
        request.setServletPath("/failing");
        assertThatThrownBy(
                        () ->
                                new RequestLoggingFilter()
                                        .doFilter(
                                                request,
                                                new MockHttpServletResponse(),
                                                (r, s) -> {
                                                    throw new IllegalStateException(
                                                            "fictitious-secret");
                                                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/css/app.css", "/js/app.js", "/images/logo.svg"})
    void logging_WithStaticResource_DoesNotAddCorrelation(String path) throws Exception {
        var req = new MockHttpServletRequest("GET", path);
        req.setServletPath(path);
        var res = new MockHttpServletResponse();
        new RequestLoggingFilter().doFilter(req, res, (r, s) -> {});
        assertThat(res.getHeader("X-Request-ID")).isNull();
    }

    @Test
    void upload_WithTooLargeRequest_RedirectsWithoutInvokingController() throws Exception {
        var resolver = mock(MultipartResolver.class);
        var chain = mock(jakarta.servlet.FilterChain.class);
        var request = new MockHttpServletRequest("POST", "/register/institution");
        request.setServletPath("/register/institution");
        when(resolver.isMultipart(request)).thenReturn(true);
        when(resolver.resolveMultipart(request)).thenThrow(new MaxUploadSizeExceededException(5));
        var response = new MockHttpServletResponse();
        new RegistrationUploadLimitFilter(resolver).doFilter(request, response, chain);
        assertThat(response.getRedirectedUrl()).isEqualTo("/register/institution?uploadError");
        verifyNoInteractions(chain);
    }

    @Test
    void upload_WithResolvedRequest_CleansTemporaryMultipart() throws Exception {
        var resolver = mock(MultipartResolver.class);
        var chain = mock(jakarta.servlet.FilterChain.class);
        var request = new MockHttpServletRequest("POST", "/register/institution");
        request.setServletPath("/register/institution");
        var multipart = mock(MultipartHttpServletRequest.class);
        when(resolver.isMultipart(request)).thenReturn(true);
        when(resolver.resolveMultipart(request)).thenReturn(multipart);
        new RegistrationUploadLimitFilter(resolver)
                .doFilter(request, new MockHttpServletResponse(), chain);
        verify(chain).doFilter(eq(multipart), any());
        verify(resolver).cleanupMultipart(multipart);
    }

    @ParameterizedTest
    @CsvSource({"GET,/register/institution", "POST,/other"})
    void upload_WithUnrelatedRequest_DoesNotParseMultipart(String method, String path)
            throws Exception {
        var resolver = mock(MultipartResolver.class);
        var request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        new RegistrationUploadLimitFilter(resolver)
                .doFilter(request, new MockHttpServletResponse(), (r, s) -> {});
        verifyNoInteractions(resolver);
    }
}
