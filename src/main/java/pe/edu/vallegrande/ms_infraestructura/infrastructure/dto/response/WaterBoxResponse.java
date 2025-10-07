package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WaterBoxResponse {
    private Long id;
    private String organizationId;
    private String boxCode;
    private BoxType boxType;
    private LocalDate installationDate;
    private Long currentAssignmentId;
    private Status status;
    private LocalDateTime createdAt;
}