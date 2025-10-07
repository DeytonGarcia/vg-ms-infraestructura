package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WaterBoxAssignmentRequest {
    @NotNull(message = "El ID de la caja de agua no puede ser nulo.")
    private Long waterBoxId;

    @NotBlank(message = "El ID de usuario no puede estar vacío.")
    private String userId;

    @NotNull(message = "La fecha de inicio no puede ser nula.")
    private LocalDateTime startDate;

    @NotNull(message = "La tarifa mensual no puede ser nula.")
    @DecimalMin(value = "0.01", message = "La tarifa mensual debe ser mayor que cero.")
    private BigDecimal monthlyFee;
}