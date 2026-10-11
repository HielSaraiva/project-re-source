package edu.br.resource.resourcesystem.config;

import edu.br.resource.resourcesystem.controller.error.ErrorResponses;
import edu.br.resource.resourcesystem.security.GoogleOidcUserService;
import edu.br.resource.resourcesystem.security.ProfileAuthenticationSuccessHandler;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    org.springframework.security.web.session.HttpSessionEventPublisher sessionEvents() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain security(
            HttpSecurity http,
            ProfileAuthenticationSuccessHandler success,
            GoogleOidcUserService googleUsers,
            ObjectProvider<ClientRegistrationRepository> clients,
            org.springframework.security.core.session.SessionRegistry sessions)
            throws Exception {
        http.authorizeHttpRequests(
                        a ->
                                a.dispatcherTypeMatchers(DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(
                                                "/",
                                                "/sobre",
                                                "/sustentabilidade",
                                                "/politica-de-privacidade",
                                                "/login",
                                                "/password/forgot",
                                                "/password/reset",
                                                "/register/donor",
                                                "/register/institution",
                                                "/register/institution/cnpj",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/error")
                                        .permitAll()
                                        .requestMatchers("/donor/**")
                                        .hasRole("DONOR")
                                        .requestMatchers("/ong/**")
                                        .hasRole("ONG")
                                        .requestMatchers("/api/notifications/**")
                                        .hasAnyRole("DONOR", "ONG")
                                        .requestMatchers(
                                                "/api/**",
                                                "/actuator/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/oauth2/**",
                                                "/login/oauth2/**")
                                        .authenticated()
                                        .anyRequest()
                                        .permitAll())
                .formLogin(
                        f ->
                                f.loginPage("/login")
                                        .usernameParameter("identifier")
                                        .successHandler(success)
                                        .failureHandler(
                                                (request, response, exception) -> {
                                                    if (exception
                                                            instanceof
                                                            AuthenticationServiceException) {
                                                        response.sendError(503);
                                                        return;
                                                    }
                                                    response.sendRedirect(
                                                            request.getContextPath()
                                                                    + "/login?error&profile="
                                                                    + ("ong"
                                                                                    .equals(
                                                                                            request
                                                                                                    .getParameter(
                                                                                                            "profile"))
                                                                            ? "ong"
                                                                            : "donor"));
                                                })
                                        .permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll())
                .exceptionHandling(
                        e ->
                                e.accessDeniedHandler(
                                                (request, response, exception) -> {
                                                    if (exception instanceof CsrfException)
                                                        request.setAttribute(
                                                                ErrorResponses.CSRF_ERROR, true);
                                                    response.sendError(403);
                                                })
                                        .defaultAuthenticationEntryPointFor(
                                                (request, response, exception) ->
                                                        response.sendError(401),
                                                request ->
                                                        request.getServletPath().startsWith("/api/")
                                                                || request.getServletPath()
                                                                        .startsWith("/donor/api/")
                                                                || request.getServletPath()
                                                                        .startsWith("/ong/api/")))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(
                        s ->
                                s.maximumSessions(-1)
                                        .sessionRegistry(sessions)
                                        .expiredUrl("/login?sessionExpired"));
        if (clients.getIfAvailable() != null) {
            http.oauth2Login(
                    o ->
                            o.loginPage("/login")
                                    .userInfoEndpoint(u -> u.oidcUserService(googleUsers))
                                    .successHandler(success)
                                    .failureUrl("/login?oauthError")
                                    .permitAll());
        }
        return http.build();
    }
}
