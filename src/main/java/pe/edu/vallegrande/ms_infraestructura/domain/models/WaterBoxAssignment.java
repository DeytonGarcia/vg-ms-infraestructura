package pe.edu.vallegrande.ms_infraestructura.domain.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("water_box_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBoxAssignment {
    @Id
    private Long id;

    @Column("water_box_id")
    private Long waterBoxId;

    @Column("user_id")
    private String userId;

    @Column("start_date")
    private LocalDateTime startDate;

    @Column("end_date")
    private LocalDateTime endDate;

    @Column("monthly_fee")
    private BigDecimal monthlyFee;

    @Column("status")
    private Status status;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("transfer_id")
    private Long transferId;
}