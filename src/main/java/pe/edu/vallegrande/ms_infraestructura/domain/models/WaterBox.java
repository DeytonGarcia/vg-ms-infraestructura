package pe.edu.vallegrande.ms_infraestructura.domain.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_boxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WaterBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    @Column(name = "id", updatable = false, nullable = false)
    private Long id; // ID numérico

    @Column(name = "organization_id", nullable = false)
    private String organizationId; // Cambiado a String para coincidir con los DTOs

    @Column(name = "box_code", nullable = false, unique = true, length = 50)
    private String boxCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "box_type", nullable = false, length = 20)
    private BoxType boxType;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    // Referencia a la asignación actual (puede ser nula si no está asignada)
    @Column(name = "current_assignment_id")
    private Long currentAssignmentId; // ID numérico de la asignación

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}