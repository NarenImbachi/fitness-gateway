package com.fitness.gateway.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.fitness.gateway.dto.RegisterRequest;
import com.fitness.gateway.service.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    
    private final UserService userService;

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
    }
}
