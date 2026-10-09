package edu.br.resource.resourcesystem.config;

import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

    @Bean
    UserDetailsService databaseUsers(UserRepository users, InstitutionRepository institutions) {
        return email -> {
            var donor = users.findByEmailIgnoreCase(email);
            var ong = institutions.findByEmailIgnoreCase(email);
            if (donor.isPresent() && ong.isPresent()) throw new UsernameNotFoundException("E-mail ambíguo entre contas.");
            if (donor.isPresent()) {
                var u = donor.get();
                if (u.getPasswordHash() == null) throw new UsernameNotFoundException("Conta sem senha local.");
                return User.withUsername(u.getEmail()).password(encoded(u.getPasswordHash())).roles(u.getRole() == AccountRole.DONOR ? "DONOR" : "ADMINISTRATOR")
                        .disabled(u.getStatus() != AccountStatus.ACTIVE).build();
            }
            var i = ong.orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada."));
            return User.withUsername(i.getEmail()).password(encoded(i.getPasswordHash())).roles("ONG").disabled(i.getStatus() != InstitutionStatus.APPROVED).build();
        };
    }
    private static String encoded(String hash) { return hash.startsWith("$") ? "{bcrypt}" + hash : hash; }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a.requestMatchers("/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/donor/**").hasRole("DONOR").requestMatchers("/ong/**").hasRole("ONG").anyRequest().authenticated())
                .formLogin(f -> f.successHandler((request, response, authentication) -> response.sendRedirect(request.getContextPath()
                        + (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ONG")) ? "/ong/dashboard" : "/donor/dashboard"))))
                .httpBasic(Customizer.withDefaults()).build();
    }
}
