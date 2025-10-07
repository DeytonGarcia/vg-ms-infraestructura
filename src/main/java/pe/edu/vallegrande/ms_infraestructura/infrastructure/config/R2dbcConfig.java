package pe.edu.vallegrande.ms_infraestructura.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración R2DBC para el proyecto reactivo
 * Los enums se manejan automáticamente por Spring Data R2DBC
 */
@Configuration
public class R2dbcConfig {
    // Spring Boot maneja automáticamente la configuración R2DBC
    // Los enums se convierten automáticamente a String y viceversa
}