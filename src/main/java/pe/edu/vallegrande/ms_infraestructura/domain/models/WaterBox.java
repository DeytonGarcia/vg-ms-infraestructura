package pe.edu.vallegrande.ms_infraestructura.domain.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("water_boxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBox {
    @Id
    private Long id;

    @Column("organization_id")
    private String organizationId;

    @Column("box_code")
    private String boxCode;

    @Column("box_type")
    private BoxType boxType;

    @Column("installation_date")
    private LocalDate installationDate;

    @Column("current_assignment_id")
    private Long currentAssignmentId;

    @Column("status")
    private Status status;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}