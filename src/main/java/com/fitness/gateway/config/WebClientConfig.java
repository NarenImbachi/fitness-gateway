package com.fitness.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * WebClient.Builder bean es usado para crear instancias de WebClient, que es una herramienta reactiva para hacer solicitudes HTTP. 
     * La anotación @LoadBalanced permite que el WebClient utilice el balanceo de carga para resolver 
     * los nombres de servicio a través de un servicio de descubrimiento (como Eureka).
     * @return
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://userservice") // El nombre del servicio registrado en Eureka
                .build();
    }
}
