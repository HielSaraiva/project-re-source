package edu.br.resource.resourcesystem.config;

import edu.br.resource.resourcesystem.security.GoogleOidcUserService;
import edu.br.resource.resourcesystem.security.ProfileAuthenticationSuccessHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, ProfileAuthenticationSuccessHandler success,
            GoogleOidcUserService googleUsers, ObjectProvider<ClientRegistrationRepository> clients) throws Exception {
        http
                .authorizeHttpRequests(a -> a.requestMatchers("/", "/login", "/register/donor", "/register/institution", "/register/institution/cnpj", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/donor/**").hasRole("DONOR").requestMatchers("/ong/**").hasRole("ONG")
                        .anyRequest().authenticated())
                .formLogin(f -> f.loginPage("/login").usernameParameter("identifier")
                        .successHandler(success).failureHandler((request, response, exception) ->
                                response.sendRedirect(request.getContextPath() + "/login?error&profile="
                                        + ("ong".equals(request.getParameter("profile")) ? "ong" : "donor")))
                        .permitAll())
                .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll())
                .httpBasic(Customizer.withDefaults());
        if (clients.getIfAvailable() != null) {
            http.oauth2Login(o -> o.loginPage("/login").userInfoEndpoint(u -> u.oidcUserService(googleUsers))
                    .successHandler(success).failureUrl("/login?oauthError").permitAll());
        }
        return http.build();
    }
}
