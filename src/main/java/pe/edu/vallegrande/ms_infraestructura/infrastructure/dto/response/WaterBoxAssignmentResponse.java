package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WaterBoxAssignmentResponse {
    private Long id;
    private Long waterBoxId;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal monthlyFee;
    private Status status;
    private LocalDateTime createdAt;
    private Long transferId;
}