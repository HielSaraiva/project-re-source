package edu.br.resource.resourcesystem.controller.error;

import edu.br.resource.resourcesystem.config.PrivacyProperties;
import edu.br.resource.resourcesystem.observability.RequestLoggingFilter;
import edu.br.resource.resourcesystem.security.ProfileAuthenticationSuccessHandler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class ErrorResponses {
    public static final String CSRF_ERROR = ErrorResponses.class.getName() + ".csrf";
    private final PrivacyProperties privacy;

    private record Content(String title, String description, String icon) {}

    public Object respond(int status, HttpServletRequest request, HttpServletResponse response) {
        if (status < 400 || status > 599) status = 500;
        Content content = content(status, Boolean.TRUE.equals(request.getAttribute(CSRF_ERROR)));
        response.setStatus(status);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("X-Robots-Tag", "noindex, nofollow");
        response.setHeader("Referrer-Policy", "no-referrer");
        if (status == 503) response.setHeader(HttpHeaders.RETRY_AFTER, "30");
        Object id = request.getAttribute(RequestLoggingFilter.REQUEST_ID_HEADER);
        String requestId =
                id instanceof String value
                        ? value
                        : response.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER);
        if (isJsonRequest(request)) {
            ProblemDetail problem =
                    ProblemDetail.forStatusAndDetail(
                            HttpStatusCode.valueOf(status), content.description());
            problem.setTitle(content.title());
            if (requestId != null) problem.setProperty("requestId", requestId);
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(problem);
        }
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean signedIn =
                auth != null
                        && auth.isAuthenticated()
                        && !new AuthenticationTrustResolverImpl().isAnonymous(auth);
        String home = signedIn ? ProfileAuthenticationSuccessHandler.destination(auth) : "/";
        boolean hasDashboard = signedIn && !"/sobre".equals(home);
        var page = new ModelAndView("error/page");
        page.setStatus(HttpStatusCode.valueOf(status));
        page.addObject("errorStatus", status);
        page.addObject("errorTitle", content.title());
        page.addObject("errorDescription", content.description());
        page.addObject("errorIcon", content.icon());
        page.addObject("errorHomePath", home);
        page.addObject(
                "errorHomeLabel", hasDashboard ? "Voltar ao meu painel" : "Voltar ao início");
        page.addObject("errorRequestId", requestId);
        page.addObject("errorContactEmail", privacy.contactEmail());
        page.addObject(
                "errorSessionExpired", Boolean.TRUE.equals(request.getAttribute(CSRF_ERROR)));
        page.addObject("publicSection", "error");
        return page;
    }

    public static boolean isJsonRequest(HttpServletRequest request) {
        Object original = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = original instanceof String value ? value : request.getRequestURI();
        path = path.substring(Math.min(request.getContextPath().length(), path.length()));
        if (path.startsWith("/api/")
                || path.startsWith("/donor/api/")
                || path.startsWith("/ong/api/")) return true;
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null
                && !accept.contains("text/html")
                && (accept.contains("application/json")
                        || accept.contains("application/problem+json"));
    }

    private Content content(int status, boolean csrf) {
        if (status == 403 && csrf)
            return new Content(
                    "Vamos retomar com segurança.",
                    "A sessão deste formulário expirou ou não pôde ser validada. Abra a página"
                        + " novamente antes de tentar enviar. Por segurança, as senhas e os anexos"
                        + " precisam ser informados outra vez.",
                    "shield");
        return switch (status) {
            case 401 ->
                    new Content(
                            "Entre para continuar.",
                            "Esta ação precisa de uma sessão válida. Entre na sua conta para"
                                    + " continuar com segurança.",
                            "lock");
            case 403 ->
                    new Content(
                            "Este acesso não está disponível.",
                            "Sua conta não possui permissão para acessar esta página ou realizar"
                                    + " esta ação. Volte ao seu painel para continuar nos caminhos"
                                    + " disponíveis para você.",
                            "shield");
            case 404 ->
                    new Content(
                            "Esse caminho não foi encontrado.",
                            "A página que você procura pode ter mudado de endereço ou o link pode"
                                    + " estar incompleto. Você pode continuar a partir do início.",
                            "search");
            case 409 ->
                    new Content(
                            "Os dados foram atualizados.",
                            "Outra operação alterou os dados desta solicitação. Consulte o"
                                    + " andamento antes de tentar novamente.",
                            "file-text");
            case 429 ->
                    new Content(
                            "Vamos dar um pequeno intervalo.",
                            "Recebemos muitas solicitações em pouco tempo. Aguarde um momento antes"
                                    + " de tentar novamente.",
                            "shield");
            case 500 ->
                    new Content(
                            "Não foi possível concluir agora.",
                            "Ocorreu um problema ao processar sua solicitação. Se você estava"
                                + " enviando dados, consulte o andamento antes de repetir a ação.",
                            "file-text");
            case 503 ->
                    new Content(
                            "Precisamos de um momento.",
                            "O serviço está temporariamente indisponível. Aguarde um pouco e acesse"
                                + " novamente. Se você estava enviando dados, confira o andamento"
                                + " antes de repetir a ação.",
                            "file-text");
            default ->
                    status >= 500
                            ? new Content(
                                    "Não foi possível concluir agora.",
                                    "O serviço encontrou um problema ao processar a solicitação."
                                            + " Aguarde um pouco e consulte o andamento antes de"
                                            + " repetir a ação.",
                                    "file-text")
                            : new Content(
                                    "Vamos rever esta solicitação.",
                                    "Não foi possível processar a solicitação nesse formato. Abra a"
                                            + " página novamente e confira as informações antes de"
                                            + " enviar.",
                                    "file-text");
        };
    }
}
