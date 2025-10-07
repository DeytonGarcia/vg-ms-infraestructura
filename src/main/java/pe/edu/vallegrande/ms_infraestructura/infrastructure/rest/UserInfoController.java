package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.service.ReactiveJwtService;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador de ejemplo para mostrar cómo extraer información del JWT
 * Este controlador demuestra el uso del ReactiveJwtService
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserInfoController {

    private final ReactiveJwtService jwtService;

    /**
     * Obtiene información básica del usuario actual
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Mono<ReactiveJwtService.UserInfo> getCurrentUser() {
        return jwtService.getCurrentUserInfo();
    }

    /**
     * Obtiene solo el ID del usuario actual
     */
    @GetMapping("/me/id")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getCurrentUserId() {
        return jwtService.getCurrentUserId();
    }

    /**
     * Obtiene solo el username del usuario actual
     */
    @GetMapping("/me/username")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getCurrentUsername() {
        return jwtService.getCurrentUsername();
    }

    /**
     * Obtiene solo el email del usuario actual
     */
    @GetMapping("/me/email")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getCurrentUserEmail() {
        return jwtService.getCurrentUserEmail();
    }

    /**
     * Obtiene los roles del usuario actual
     */
    @GetMapping("/me/roles")
    @PreAuthorize("isAuthenticated()")
    public Mono<List<String>> getCurrentUserRoles() {
        return jwtService.getCurrentUserRoles();
    }

    /**
     * Verifica si el usuario actual tiene rol de ADMIN
     */
    @GetMapping("/me/is-admin")
    @PreAuthorize("isAuthenticated()")
    public Mono<Boolean> isCurrentUserAdmin() {
        return jwtService.hasRole("ADMIN");
    }

    /**
     * Verifica si el usuario actual tiene rol de SUPER_ADMIN
     */
    @GetMapping("/me/is-super-admin")
    @PreAuthorize("isAuthenticated()")
    public Mono<Boolean> isCurrentUserSuperAdmin() {
        return jwtService.hasRole("SUPER_ADMIN");
    }

    /**
     * Endpoint solo para ADMINs - demuestra autorización por rol
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<String> adminOnlyEndpoint() {
        return jwtService.getCurrentUsername()
                .map(username -> "Hola " + username + ", tienes acceso de ADMIN!");
    }

    /**
     * Endpoint solo para SUPER_ADMINs - demuestra autorización por rol
     */
    @GetMapping("/super-admin-only")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<String> superAdminOnlyEndpoint() {
        return jwtService.getCurrentUsername()
                .map(username -> "Hola " + username + ", tienes acceso de SUPER_ADMIN!");
    }

    /**
     * Endpoint que verifica múltiples roles
     */
    @GetMapping("/admin-or-client")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public Mono<String> adminOrClientEndpoint() {
        return jwtService.getCurrentUserInfo()
                .map(userInfo -> String.format(
                    "Hola %s, tienes uno de estos roles: %s", 
                    userInfo.getUsername(), 
                    userInfo.getRoles()
                ));
    }
}