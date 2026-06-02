package com.santimpay.posctl.shared.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless OAuth2 Resource Server validating Keycloak-issued JWTs.
 *
 * <p>Coarse access is route-based here; fine-grained {@code resource:action} checks happen in the
 * application layer via {@code @PreAuthorize("hasAuthority('PERM_merchant:approve')")} or
 * {@link CurrentUser#hasPermission}. Keycloak realm/client roles AND a {@code permissions} claim are
 * both mapped to Spring authorities (roles as {@code ROLE_*}, permissions as {@code PERM_*}).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // stateless API, no cookies for auth
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health/**",
                    "/actuator/prometheus",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/health/reports", "/api/v1/health/reports:bulk")
                    .hasAuthority("PERM_device:telemetry")
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return converter;
    }

    @SuppressWarnings("unchecked")
    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Keycloak realm roles -> ROLE_*
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }

        // Fine-grained permissions claim -> PERM_*
        List<String> perms = jwt.getClaimAsStringList("permissions");
        if (perms != null) {
            authorities.addAll(perms.stream()
                .map(p -> new SimpleGrantedAuthority("PERM_" + p))
                .collect(Collectors.toSet()));
        }
        return authorities;
    }
}
