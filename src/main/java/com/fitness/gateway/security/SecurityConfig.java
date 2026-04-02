package com.fitness.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configuracion de seguridad para el gateway, se deshabilita csrf, se permite el acceso a los endpoints de actuator y se requiere autenticacion para cualquier otro endpoint, ademas se configura el gateway como un recurso protegido por JWT.
     * @param http ServerHttpSecurity para configurar la seguridad del gateway
     * @return SecurityWebFilterChain con la configuracion de seguridad aplicada
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)                             // Deshabilitar CSRF para el gateway
                .authorizeExchange(exchange -> exchange 
                .pathMatchers("/actuator/*").permitAll()                                // Permitir acceso a endpoints de actuator
                .anyExchange().authenticated())                                         // Requerir autenticacion para cualquier otro endpoint
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))  // Configurar el gateway como un recurso protegido por JWT
                .build();
    }
}
