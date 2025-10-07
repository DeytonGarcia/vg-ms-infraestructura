package pe.edu.vallegrande.ms_infraestructura.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReactiveJwtService {

    /**
     * Obtiene el JWT del contexto de seguridad reactivo
     */
    public Mono<Jwt> getCurrentJwt() {
        return ReactiveSecurityContextHolder.getContext()
                .cast(org.springframework.security.core.context.SecurityContext.class)
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .doOnNext(jwt -> log.debug("JWT obtenido del contexto: {}", jwt.getSubject()));
    }

    /**
     * Obtiene el ID del usuario del JWT
     */
    public Mono<String> getCurrentUserId() {
        return getCurrentJwt()
                .map(Jwt::getSubject)
                .doOnNext(userId -> log.debug("User ID extraído: {}", userId));
    }

    /**
     * Obtiene el username preferido del JWT
     */
    public Mono<String> getCurrentUsername() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("preferred_username"))
                .doOnNext(username -> log.debug("Username extraído: {}", username));
    }

    /**
     * Obtiene el email del JWT
     */
    public Mono<String> getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("email"))
                .doOnNext(email -> log.debug("Email extraído: {}", email));
    }

    /**
     * Obtiene el nombre completo del JWT
     */
    public Mono<String> getCurrentUserFullName() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("name"))
                .doOnNext(name -> log.debug("Nombre completo extraído: {}", name));
    }

    /**
     * Obtiene los roles del realm_access del JWT
     */
    @SuppressWarnings("unchecked")
    public Mono<List<String>> getCurrentUserRoles() {
        return getCurrentJwt()
                .map(jwt -> {
                    try {
                        Object realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess instanceof Map) {
                            Map<String, Object> realmMap = (Map<String, Object>) realmAccess;
                            Object roles = realmMap.get("roles");
                            if (roles instanceof List) {
                                return (List<String>) roles;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error extrayendo roles de realm_access: {}", e.getMessage());
                    }
                    return List.<String>of();
                })
                .doOnNext(roles -> log.debug("Roles extraídos: {}", roles));
    }

    /**
     * Verifica si el usuario actual tiene un rol específico
     */
    public Mono<Boolean> hasRole(String role) {
        return getCurrentUserRoles()
                .map(roles -> roles.contains(role))
                .doOnNext(hasRole -> log.debug("Usuario tiene rol '{}': {}", role, hasRole));
    }

    /**
     * Verifica si el usuario actual tiene alguno de los roles especificados
     */
    public Mono<Boolean> hasAnyRole(String... roles) {
        return getCurrentUserRoles()
                .map(userRoles -> {
                    for (String role : roles) {
                        if (userRoles.contains(role)) {
                            return true;
                        }
                    }
                    return false;
                })
                .doOnNext(hasAnyRole -> log.debug("Usuario tiene alguno de los roles {}: {}", List.of(roles), hasAnyRole));
    }

    /**
     * Obtiene información completa del usuario actual
     */
    public Mono<UserInfo> getCurrentUserInfo() {
        return getCurrentJwt()
                .map(jwt -> {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(jwt.getSubject());
                    userInfo.setUsername(jwt.getClaimAsString("preferred_username"));
                    userInfo.setEmail(jwt.getClaimAsString("email"));
                    userInfo.setFullName(jwt.getClaimAsString("name"));
                    userInfo.setGivenName(jwt.getClaimAsString("given_name"));
                    userInfo.setFamilyName(jwt.getClaimAsString("family_name"));
                    
                    // Extraer roles
                    try {
                        Object realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess instanceof Map) {
                            Map<String, Object> realmMap = (Map<String, Object>) realmAccess;
                            Object roles = realmMap.get("roles");
                            if (roles instanceof List) {
                                userInfo.setRoles((List<String>) roles);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error extrayendo roles: {}", e.getMessage());
                        userInfo.setRoles(List.of());
                    }
                    
                    return userInfo;
                })
                .doOnNext(userInfo -> log.debug("UserInfo completa extraída: {}", userInfo));
    }

    /**
     * Clase para encapsular la información del usuario
     */
    public static class UserInfo {
        private String userId;
        private String username;
        private String email;
        private String fullName;
        private String givenName;
        private String familyName;
        private List<String> roles;

        // Getters y Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }

        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "userId='" + userId + '\'' +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", roles=" + roles +
                    '}';
        }
    }
}