package com.fitness.gateway.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final WebClient userServiceWebClient;

    public Mono<Boolean> isUserValid(String userId) {
        log.info("Calling user validation API for userId: {}", userId);
            // Realiza una solicitud GET al User Service para verificar si el usuario existe
            return userServiceWebClient.get()
                    .uri("/api/users/{id}/validate", userId)
                    .retrieve() // Recupera la respuesta de la solicitud
                    .bodyToMono(Boolean.class) // Convierte el cuerpo de la respuesta a un Mono<Boolean>
                    .onErrorResume(WebClientResponseException.class, e -> {
                        if(e.getStatusCode() == HttpStatus.NOT_FOUND) 
                            return Mono.error(new RuntimeException("User not found"));
                        else if(e.getStatusCode() == HttpStatus.BAD_REQUEST) 
                            return Mono.error(new RuntimeException("Invalid user ID format"));
                        else
                            return Mono.error(new RuntimeException("An error occurred while validating the user"));
                    });
    }

    public Mono<Void> registerUser(com.fitness.gateway.dto.RegisterRequest registerRequest) {
        log.info("Calling user registration API for email: {}", registerRequest.getEmail());
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(registerRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if(e.getStatusCode() == HttpStatus.BAD_REQUEST) 
                        return Mono.error(new RuntimeException("Invalid registration data"));
                    else
                        return Mono.error(new RuntimeException("An error occurred while registering the user"));
                });
    }
}
