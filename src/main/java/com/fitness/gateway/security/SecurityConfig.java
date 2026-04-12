package com.fitness.gateway.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebFluxSecurity // Habilita WebFlux Security (porque Gateway usa Project Reactor)
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
                    .pathMatchers("/api/users/{userId}/validate").permitAll() 
                .pathMatchers("/api/users/register").permitAll() 
                .pathMatchers("/actuator/*").permitAll()                                // Permitir acceso a endpoints de actuator
                .anyExchange().authenticated())                                         // Requerir autenticacion para cualquier otro endpoint
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))  // Configurar el gateway como un recurso protegido por JWT
                .build();
    }

    /**
     * Configuracion de CORS para el gateway, se permite el acceso desde el frontend (http://localhost:5173) 
     * y se permiten los metodos GET, POST, PUT, DELETE y OPTIONS, ademas se permiten los headers Authorization, Content-Type y X-User-ID.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-User-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        //source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
