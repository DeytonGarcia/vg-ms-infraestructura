package pe.edu.vallegrande.ms_infraestructura.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.service.JwtService;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class SecurityConfig {

    private final JwtService jwtService;
    
    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    // JwtDecoder beans moved to JwtConfig
    
    // Rutas públicas que no requieren autenticación
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/actuator/**",
            "/api/public/**"
    );

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_PATHS.toArray(new String[0])).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                            .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }
    
    // JwtDecoder beans moved to JwtConfig
    
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            // Extraer roles de realm_access.roles (estándar de Keycloak)
            List<String> realmRoles = extractRealmRoles(jwt);
            realmRoles.forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                log.debug("Agregado rol de realm: ROLE_{}", role);
                addPermissionsByRole(authorities, role);
            });
            
            // También extraer de custom claims si existen 
            String customRole = jwt.getClaimAsString("role"); 
            if (customRole != null) { 
                authorities.add(new SimpleGrantedAuthority("ROLE_" + customRole)); 
                log.debug("Agregado rol personalizado: ROLE_{}", customRole); 
                addPermissionsByRole(authorities, customRole); 
            } 
            
            List<String> permissions = extractPermissionsList(jwt); 
            if (permissions != null) { 
                permissions.forEach(permission -> { 
                    authorities.add(new SimpleGrantedAuthority(permission)); 
                    log.debug("Agregado permiso: {}", permission); 
                }); 
            } 
            
            log.info("Usuario {} autenticado con {} authorities", jwt.getSubject(), authorities.size()); 
            return authorities; 
        }); 
        
        return new ReactiveJwtAuthenticationConverterAdapter(converter); 
    }
    
    /** 
     * Método auxiliar para agregar permisos automáticamente según el rol 
     */ 
    private void addPermissionsByRole(Set<GrantedAuthority> authorities, String role) { 
        switch (role) { 
            case "SUPER_ADMIN" -> { 
                // Permisos del SUPER_ADMIN 
                authorities.add(new SimpleGrantedAuthority("CREATE_ADMIN")); 
                authorities.add(new SimpleGrantedAuthority("UPDATE_ADMIN")); 
                authorities.add(new SimpleGrantedAuthority("DELETE_ADMIN")); 
                authorities.add(new SimpleGrantedAuthority("VIEW_ALL_ADMINS")); 
                authorities.add(new SimpleGrantedAuthority("MANAGE_SYSTEM")); 
                authorities.add(new SimpleGrantedAuthority("DELETE_ANY_USER")); 
                log.debug("Agregados permisos de SUPER_ADMIN"); 
            } 
            case "ADMIN" -> { 
                // Permisos del ADMIN 
                authorities.add(new SimpleGrantedAuthority("CREATE_CLIENT")); 
                authorities.add(new SimpleGrantedAuthority("UPDATE_CLIENT")); 
                authorities.add(new SimpleGrantedAuthority("DELETE_CLIENT")); 
                authorities.add(new SimpleGrantedAuthority("VIEW_ORG_CLIENTS")); 
                authorities.add(new SimpleGrantedAuthority("MANAGE_ORG")); 
                authorities.add(new SimpleGrantedAuthority("RESTORE_CLIENT")); 
                log.debug("Agregados permisos de ADMIN"); 
            } 
            case "CLIENT" -> { 
                // Permisos del CLIENT 
                authorities.add(new SimpleGrantedAuthority("VIEW_OWN_PROFILE")); 
                authorities.add(new SimpleGrantedAuthority("UPDATE_OWN_PROFILE")); 
                log.debug("Agregados permisos de CLIENT"); 
            } 
            default -> log.debug("Rol {} no tiene permisos predefinidos", role); 
        } 
    } 
    
    /** 
     * Método auxiliar para extraer roles de realm_access.roles de Keycloak 
     */ 
    @SuppressWarnings("unchecked") 
    private List<String> extractRealmRoles(Jwt jwt) { 
        try { 
            Object realmAccess = jwt.getClaim("realm_access"); 
            if (realmAccess instanceof java.util.Map) { 
                java.util.Map<String, Object> realmMap = (java.util.Map<String, Object>) realmAccess; 
                Object roles = realmMap.get("roles"); 
                if (roles instanceof List) { 
                    return (List<String>) roles; 
                } 
            } 
        } catch (Exception e) { 
            log.warn("Error extrayendo roles de realm_access: {}", e.getMessage()); 
        } 
        return List.of(); 
    } 
    
    /** 
     * Método auxiliar para extraer lista de permisos del JWT 
     */ 
    @SuppressWarnings("unchecked") 
    private List<String> extractPermissionsList(Jwt jwt) { 
        Object permissionsObj = jwt.getClaim("permissions"); 
        
        if (permissionsObj == null) { 
            return List.of(); 
        } 
        
        if (permissionsObj instanceof List) { 
            try { 
                return (List<String>) permissionsObj; 
            } catch (ClassCastException e) { 
                log.warn("No se pudo convertir permissions a List<String>: {}", e.getMessage()); 
                return List.of(); 
            } 
        } 
        
        if (permissionsObj instanceof String) { 
            String permissionsStr = (String) permissionsObj; 
            return Arrays.asList(permissionsStr.split(",")); 
        } 
        
        log.warn("Tipo de permissions no soportado: {}", permissionsObj.getClass()); 
        return List.of(); 
    }
}