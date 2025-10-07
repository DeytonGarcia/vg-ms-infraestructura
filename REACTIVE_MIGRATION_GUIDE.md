# Guía de Migración a Spring Boot Reactivo (WebFlux)

## Resumen de Cambios

Este proyecto ha sido migrado de Spring Boot tradicional (Spring MVC + JPA) a Spring Boot Reactivo (WebFlux + R2DBC) para mejorar el rendimiento y la escalabilidad.

## Principales Cambios Realizados

### 1. Dependencias Actualizadas

**Antes (Spring MVC + JPA):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

**Después (WebFlux + R2DBC):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
</dependency>
```

### 2. Configuración de Base de Datos

**Antes (JPA):**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
  datasource:
    url: jdbc:postgresql://...
```

**Después (R2DBC):**
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://...
    pool:
      initial-size: 5
      max-size: 20
  data:
    r2dbc:
      repositories:
        enabled: true
```

### 3. Entidades Actualizadas

**Antes (JPA):**
```java
@Entity
@Table(name = "water_boxes")
public class WaterBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

**Después (R2DBC):**
```java
@Table("water_boxes")
public class WaterBox {
    @Id
    private Long id;
    
    @Column("organization_id")
    private String organizationId;
    // ...
}
```

### 4. Repositorios Reactivos

**Antes (JPA):**
```java
public interface WaterBoxRepository extends JpaRepository<WaterBox, Long> {
    List<WaterBox> findByStatus(Status status);
}
```

**Después (R2DBC):**
```java
public interface WaterBoxRepository extends R2dbcRepository<WaterBox, Long> {
    Flux<WaterBox> findByStatus(Status status);
    Mono<WaterBox> findByCurrentAssignmentId(Long currentAssignmentId);
}
```

### 5. Servicios Reactivos

**Antes (Tradicional):**
```java
public interface IWaterBoxService {
    List<WaterBoxResponse> getAllActive();
    WaterBoxResponse getById(Long id);
    WaterBoxResponse save(WaterBoxRequest request);
}
```

**Después (Reactivo):**
```java
public interface IWaterBoxService {
    Flux<WaterBoxResponse> getAllActive();
    Mono<WaterBoxResponse> getById(Long id);
    Mono<WaterBoxResponse> save(WaterBoxRequest request);
}
```

### 6. Controladores Reactivos

**Antes (Spring MVC):**
```java
@GetMapping("/water-boxes/active")
public ResponseEntity<List<WaterBoxResponse>> getAllActiveWaterBoxes() {
    return ResponseEntity.ok(waterBoxService.getAllActive());
}
```

**Después (WebFlux):**
```java
@GetMapping("/water-boxes/active")
@PreAuthorize("hasRole('ADMIN')")
public Flux<WaterBoxResponse> getAllActiveWaterBoxes() {
    return waterBoxService.getAllActive();
}
```

## Nuevas Funcionalidades

### 1. Servicio JWT Reactivo

Se ha creado `ReactiveJwtService` para extraer información del token JWT de manera reactiva:

```java
@Service
public class ReactiveJwtService {
    public Mono<String> getCurrentUserId() { ... }
    public Mono<String> getCurrentUsername() { ... }
    public Mono<List<String>> getCurrentUserRoles() { ... }
    public Mono<UserInfo> getCurrentUserInfo() { ... }
}
```

### 2. Análisis del Token JWT de Keycloak

El token JWT proporcionado contiene:
```json
{
  "realm_access": {
    "roles": ["offline_access", "default-roles-sistema-jass", "uma_authorization", "ADMIN"]
  },
  "preferred_username": "javier.fatama@jass.gob.pe",
  "name": "Javier Fatama",
  "email": "jfatama@gmail.com"
}
```

### 3. Controlador de Información de Usuario

Se ha agregado `/api/user/*` endpoints para demostrar el uso del JWT:

- `GET /api/user/me` - Información completa del usuario
- `GET /api/user/me/roles` - Roles del usuario
- `GET /api/user/admin-only` - Endpoint solo para ADMINs

### 4. Manejo de Excepciones Reactivo

Se ha creado `ReactiveGlobalExceptionHandler` para manejar excepciones en el contexto reactivo.

### 5. Configuración R2DBC

Se ha agregado `R2dbcConfig` para manejar conversiones de enums y configuraciones específicas de R2DBC.

## Seguridad y Autorización

### Roles Extraídos del JWT

El sistema extrae roles de `realm_access.roles` del token Keycloak:

- **ADMIN**: Acceso completo a operaciones CRUD
- **CLIENT**: Solo operaciones de lectura
- **SUPER_ADMIN**: Acceso de gestión completo

### Autorización por Endpoint

```java
// Solo ADMINs
@PreAuthorize("hasRole('ADMIN')")

// ADMINs o CLIENTs
@PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")

// Solo SUPER_ADMINs
@PreAuthorize("hasRole('SUPER_ADMIN')")
```

## Beneficios de la Migración

### 1. **Rendimiento Mejorado**
- Operaciones no bloqueantes
- Mejor uso de recursos del sistema
- Mayor throughput

### 2. **Escalabilidad**
- Manejo eficiente de muchas conexiones concurrentes
- Menor uso de memoria por request

### 3. **Programación Reactiva**
- Composición de operaciones asíncronas
- Mejor manejo de backpressure
- Streams de datos en tiempo real

### 4. **Integración con JWT**
- Extracción reactiva de información del usuario
- Logging contextual con información del usuario
- Autorización granular por rol

## Comandos de Prueba

### 1. Compilar el Proyecto
```bash
mvn clean compile
```

### 2. Ejecutar Tests
```bash
mvn test
```

### 3. Ejecutar la Aplicación
```bash
mvn spring-boot:run
```

### 4. Construir Docker Image
```bash
docker build -t vg-ms-infrastructure-reactive .
```

### 5. Ejecutar con Docker Compose
```bash
docker-compose up
```

## Endpoints de Prueba

### Información del Usuario (requiere JWT)
```bash
# Obtener información del usuario actual
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8084/api/user/me

# Obtener roles del usuario
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8084/api/user/me/roles

# Endpoint solo para ADMINs
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8084/api/user/admin-only
```

### Cajas de Agua (requiere JWT con rol ADMIN)
```bash
# Obtener cajas activas
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8084/api/admin/water-boxes/active

# Crear nueva caja
curl -X POST \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"organizationId":"ORG001","boxCode":"BOX001","boxType":"CAÑO","installationDate":"2024-01-01"}' \
     http://localhost:8084/api/admin/water-boxes
```

## Consideraciones Importantes

### 1. **Transacciones**
R2DBC maneja transacciones de manera diferente. Se usa `@Transactional` pero el comportamiento es reactivo.

### 2. **Debugging**
Para debugging reactivo, usar `.doOnNext()`, `.doOnError()`, y `.log()` operators.

### 3. **Testing**
Los tests deben usar `StepVerifier` para verificar streams reactivos.

### 4. **Blocking Operations**
Evitar operaciones bloqueantes en el hilo reactivo. Usar `subscribeOn()` o `publishOn()` si es necesario.

## Próximos Pasos

1. **Implementar Tests Reactivos**: Crear tests usando `WebTestClient` y `StepVerifier`
2. **Métricas Reactivas**: Agregar métricas específicas para aplicaciones reactivas
3. **Streaming de Datos**: Implementar endpoints de streaming para datos en tiempo real
4. **Cache Reactivo**: Implementar cache reactivo con Redis
5. **Circuit Breaker**: Agregar resilience patterns para llamadas externas

## Documentación Adicional

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [Reactor Core](https://projectreactor.io/docs/core/release/reference/)
- [R2DBC PostgreSQL Driver](https://github.com/pgjdbc/r2dbc-postgresql)