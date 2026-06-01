package dev.amritanka.assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Production: validate JWTs from Azure AD (Entra ID).
     * Configure spring.security.oauth2.resourceserver.jwt.issuer-uri.
     */
    @Bean
    @Profile("!dev")
    public SecurityWebFilterChain secured(ServerHttpSecurity http,
                                          @Value("${assistant.security.permit-actuator:true}") boolean permitActuator) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/health", "/actuator/info",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers(permitActuator ? "/actuator/**" : "/__never").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
                .build();
    }

    /**
     * Dev profile: wide open for local testing with curl.
     */
    @Bean
    @Profile("dev")
    public SecurityWebFilterChain dev(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
    }
}
