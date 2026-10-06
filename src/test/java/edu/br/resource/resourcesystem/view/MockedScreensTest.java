package edu.br.resource.resourcesystem.view;

import edu.br.resource.resourcesystem.controller.donor.*;
import edu.br.resource.resourcesystem.controller.ong.OngDashboardController;
import edu.br.resource.resourcesystem.service.donor.DonorDashboardService;
import edu.br.resource.resourcesystem.service.donor.DonorNeedsService;
import edu.br.resource.resourcesystem.service.donor.DonorInventoryService;
import edu.br.resource.resourcesystem.service.ong.OngDashboardService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.ui.ExtendedModelMap;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MockedScreensTest {
    record Screen(Function<ExtendedModelMap, String> render, String title, String content) {}

    static Stream<Screen> screens() {
        return Stream.of(
                new Screen(model -> new DonorDashboardController(new DonorDashboardService()).dashboard(model),
                        "Início", "Instituto Esperança"),
                new Screen(model -> new DonorNeedsController(new DonorNeedsService()).needs(model),
                        "Necessidades", "Cadeira de Rodas"),
                new Screen(model -> new DonorShippingController().shipping(model),
                        "Envio da doação", "Confirmar Entrega Presencial"),
                new Screen(model -> new DonorDonationStatusController().status(model),
                        "Status da doação", "detail-text--urgent"),
                new Screen(model -> new DonorDonationCompletedController().completed(model),
                        "Doação concluída", "detail-text--success"),
                new Screen(model -> new OngDashboardController(new OngDashboardService()).dashboard(model),
                        "Painel da ONG", "ONG EcoVida"),
                new Screen(model -> new DonorDonationProposalController(new DonorNeedsService(), new DonorInventoryService()).proposal("wheelchairs", model),
                        "Propor doação", "Cadeira de Rodas"),
                new Screen(model -> new DonorDonationProposalController(new DonorNeedsService(), new DonorInventoryService()).proposal("notebooks", model),
                        "Propor doação", "Notebooks usados"),
                new Screen(model -> new DonorDonationProposalController(new DonorNeedsService(), new DonorInventoryService()).proposal("textbooks", model),
                        "Propor doação", "Você ainda não tem uma doação disponível nesta categoria"));
    }

    static Stream<Arguments> screensWithContextPath() {
        return screens().flatMap(screen -> Stream.of("", "/poc")
                .map(contextPath -> Arguments.of(screen, contextPath)));
    }

    @ParameterizedTest
    @MethodSource("screensWithContextPath")
    void rendersMockedScreensAndResolvesSharedFragments(Screen screen, String contextPath) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        var servletContext = new MockServletContext();
        var request = new MockHttpServletRequest(servletContext);
        request.setContextPath(contextPath);
        request.setRequestURI(contextPath + "/");
        var exchange = JakartaServletWebApplication.buildApplication(servletContext)
                .buildExchange(request, new MockHttpServletResponse());
        var model = new ExtendedModelMap();
        var template = screen.render().apply(model);
        var html = engine.process(template, new WebContext(exchange, Locale.forLanguageTag("pt-BR"), model));

        assertThat(html).contains("<title>ReSource | " + screen.title() + "</title>", screen.content(),
                contextPath + "/css/shared/base.css", contextPath + "/css/shared/header.css",
                contextPath + "/css/shared/mobile-navigation.css", contextPath + "/js/navigation.js", "aria-controls=\"primary-navigation\"", "id=\"primary-navigation\"")
                .doesNotContain("th:replace", "th:each", "${", "<th:block");
        assertThat(Pattern.compile("<header\\b").matcher(html).results().count()).isEqualTo(1);

        var assets = Pattern.compile("(?:href|src)=\"(" + Pattern.quote(contextPath)
                + "/(?:css|images|js)/[^\"]+)\"").matcher(html);
        int assetCount = 0;
        while (assets.find()) {
            assetCount++;
            assertThat(getClass().getResource("/static" + assets.group(1).substring(contextPath.length())))
                    .as("Asset referenced by %s: %s", template, assets.group(1)).isNotNull();
        }
        assertThat(assetCount).isGreaterThan(10);
        if (!contextPath.isEmpty()) {
            assertThat(html).doesNotContain("src=\"/images/", "href=\"/css/", "src=\"/js/",
                    "href=\"/donor/", "href=\"/ong/");
        }
    }
}
