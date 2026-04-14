# Gateway

Este servicio actúa como un **API Gateway** para toda la arquitectura de microservicios. Es el único punto de entrada para todas las solicitudes de los clientes, proporcionando una capa de abstracción y seguridad.

## Características Principales

- **Enrutamiento Dinámico:** Utiliza Spring Cloud Gateway para enrutar las solicitudes entrantes a los microservicios apropiados. Las reglas de enrutamiento se definen en el archivo `gateway.yaml` que obtiene del `configserver`. Las rutas se basan en patrones de URL (`Path`) y se redirigen a los servicios correspondientes utilizando el balanceo de carga (`lb://`).

- **Descubrimiento de Servicios:** Está integrado con Eureka, lo que le permite descubrir dinámicamente las ubicaciones (host y puerto) de las instancias de los otros microservicios (`userservice`, `activityservice`, etc.). Esto elimina la necesidad de codificar las URL de los servicios.

- **Seguridad con Keycloak:** El gateway está configurado como un "Resource Server" de OAuth2.
    - **Interceptación de Peticiones:** Intercepta todas las peticiones que llegan desde el frontend.
    - **Validación de Tokens JWT:** Valida los tokens JWT (JSON Web Tokens) emitidos por Keycloak. La configuración `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` apunta al endpoint de Keycloak donde se pueden obtener las claves públicas para verificar la firma de los tokens.
    - **Propagación de Encabezados:** Aunque no se muestra explícitamente en la configuración, una función común de un gateway en este escenario es decodificar el token JWT y pasar información del usuario (como el ID de usuario o los roles) a los microservicios downstream a través de encabezados HTTP. Esto evita que cada microservicio tenga que validar el token individualmente.

- **Punto de Entrada Único:** Simplifica la arquitectura del lado del cliente, ya que solo necesita conocer la URL del gateway (`http://localhost:8080`).

- **Configuración Centralizada:** Al igual que los otros servicios, obtiene su configuración del `configserver`.

## Dependencias Clave

- `spring-cloud-starter-gateway-server-webflux`: Proporciona la funcionalidad principal del API Gateway.
- `spring-cloud-starter-netflix-eureka-client`: Permite que el gateway se registre en Eureka y descubra otros servicios.
- `spring-boot-starter-security` y `spring-boot-starter-oauth2-resource-server`: Habilitan la integración con Keycloak para la validación de tokens.
- `spring-boot-starter-parent`: Versión `3.5.13`.
- **Java:** Versión `21`.

## Configuración (`gateway.yaml` desde `configserver`)

La configuración del gateway es un excelente ejemplo de la potencia del `configserver`.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8181/realms/fitness-oauth2/protocol/openid-connect/certs
  cloud:
    gateway:
      routes:
        - id: userservice
          uri: lb://userservice
          predicates:
            - Path=/api/users/**
        
        - id: activityservice
          uri: lb://activityservice
          predicates:
            - Path=/api/activities/**

        - id: iaservice
          uri: lb://iaservice
          predicates:
            - Path=/api/recommendations/**
```

- **`spring.security`**: Configura el gateway para que actúe como un servidor de recursos, validando los tokens JWT contra el servidor de Keycloak especificado.
- **`spring.cloud.gateway.routes`**: Define las reglas de enrutamiento. Por ejemplo, cualquier petición que comience con `/api/users/` será redirigida a una instancia del servicio `userservice`, que es descubierta a través de Eureka.
