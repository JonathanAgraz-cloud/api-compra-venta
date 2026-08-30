package com.jonypacheco.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad minima para la API y el dashboard: un solo usuario (Jony) via
 * HTTP Basic, definido por variables de entorno (DASHBOARD_USERNAME /
 * DASHBOARD_PASSWORD, ver application-dev.yml / application-prod.yml). Sin
 * default en produccion -- la app debe fallar al arrancar si faltan, igual
 * que las demas credenciales del proyecto (regla de seguridad no negociable:
 * todo endpoint expuesto debe protegerse).
 *
 * <p>CSRF deshabilitado: la API es de solo lectura (GET), no hay formularios
 * que muten estado.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${dashboard.username}") String username,
            @Value("${dashboard.password}") String password,
            PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles("USER")
                        .build());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(basic -> {
                });
        return http.build();
    }
}
