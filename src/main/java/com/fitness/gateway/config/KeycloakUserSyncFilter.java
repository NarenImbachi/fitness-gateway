package com.fitness.gateway.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements GlobalFilter, Ordered {
    
    /**
     * Filtro para agregar el ID del usuario autenticado (obtenido del token JWT) a las solicitudes que pasan por el API Gateway.
     * Este filtro se ejecuta después de que Spring Security haya autenticado al usuario, por lo que el contexto de seguridad ya estará disponible.
     * @param exchange El intercambio de la solicitud y respuesta HTTP.
     * @param chain La cadena de filtros del API Gateway.
     * @return Un Mono que indica cuándo se ha completado el procesamiento del filtro.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Obtenemos el contexto de seguridad reactivo
        return ReactiveSecurityContextHolder.getContext()
            .filter(c -> c.getAuthentication() != null) // Nos aseguramos de que haya una autenticación
            .flatMap(c -> {
                // Obtenemos el objeto de autenticación y el token JWT
                Object principal = c.getAuthentication().getPrincipal();
                if (principal instanceof Jwt) {
                    Jwt jwt = (Jwt) principal;
                    String userId = jwt.getSubject(); // 'sub' claim es el ID del usuario en Keycloak

                    // Mutamos la petición para añadir el nuevo header
                    ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-ID", userId)
                        .build();
                    
                    // Continuamos la cadena de filtros con la petición modificada
                    return chain.filter(exchange.mutate().request(request).build());
                }
                // Si no es un JWT, continuamos sin hacer nada
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange)); // Si no hay contexto de seguridad (peticiones públicas), continuamos
    }

    @Override
    public int getOrder() {
        // Se ejecuta después de los filtros de seguridad de Spring
        return 1; 
    }

    /*private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Extrae el userId y el token de los encabezados de la solicitud
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("Received request with userId: {} and token: {}", userId, token);

        RegisterRequest registerRequest = gestUserDetails(token);

        if(userId == null){
            userId = registerRequest.getKeycloakId();
        }

        if(userId != null && token != null) {
            String finalUserId = userId;
            return userService.isUserValid(userId).flatMap(exist -> {
                if(!exist){
                    return userService.registerUser(registerRequest).then(Mono.empty());
                    
                }else{
                    log.info("User already exists. Skipping synchronization.");
                    return Mono.empty();
                }
            }).then(Mono.defer(() -> {
                ServerHttpRequest mutateRequest = exchange.getRequest().mutate()
                    .header("X-User-ID", finalUserId)
                    .build();
                
                return chain.filter(exchange.mutate().request(mutateRequest).build());
            }));
        }
        return chain.filter(exchange);
    }

    private RegisterRequest gestUserDetails(String token) {
        try {
            // Elimina el prefijo "Bearer" del token
            String tokenWithOutBearer = token.replace("Bearer", "").trim();

            // Parsea el token JWT para extraer los claims
            SignedJWT signedJWT = SignedJWT.parse(tokenWithOutBearer);

            // Extrae los claims del token
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setKeycloakId(claims.getStringClaim("sub"));
            registerRequest.setPassword("dummy@123123");
            registerRequest.setFirstName(claims.getStringClaim("given_name"));
            registerRequest.setLastName(claims.getStringClaim("family_name"));

            return registerRequest;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/
}
