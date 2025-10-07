package pe.edu.vallegrande.ms_infraestructura.domain.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_box_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WaterBoxAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    @Column(name = "id", updatable = false, nullable = false)
    private Long id; // ID numérico

    @Column(name = "water_box_id", nullable = false)
    private Long waterBoxId; // ID numérico de la WaterBox

    @Column(name = "user_id", nullable = false)
    private String userId; // Cambiado a String para coincidir con los DTOs

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate; // Fecha de fin (para asignaciones inactivas)

    @Column(name = "monthly_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Referencia opcional a la transferencia que pudo haber inactivado esta asignación
    @Column(name = "transfer_id")
    private Long transferId; // ID numérico de la transferencia
}