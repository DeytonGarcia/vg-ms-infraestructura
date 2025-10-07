package pe.edu.vallegrande.ms_infraestructura.domain.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.LocalDateTime;
import java.util.List;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.StringListConverter;

@Entity
@Table(name = "water_box_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WaterBoxTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    @Column(name = "id", updatable = false, nullable = false)
    private Long id; // ID numérico

    @Column(name = "water_box_id", nullable = false)
    private Long waterBoxId; // ID numérico de la WaterBox

    @Column(name = "old_assignment_id", nullable = false)
    private Long oldAssignmentId; // ID numérico de la asignación anterior

    @Column(name = "new_assignment_id", nullable = false)
    private Long newAssignmentId; // ID numérico de la nueva asignación

    @Column(name = "transfer_reason", nullable = false, length = 255)
    private String transferReason;

    @Column(name = "documents", columnDefinition = "text") // Usamos TEXT para almacenar la lista como String
    @Convert(converter = StringListConverter.class)
    private List<String> documents; // Almacenamos como String directamente

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}