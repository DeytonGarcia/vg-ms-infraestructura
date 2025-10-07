package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WaterBoxTransferRequest {
    @NotNull(message = "El ID de la caja de agua no puede ser nulo.")
    private Long waterBoxId;

    @NotNull(message = "El ID de la asignación anterior no puede ser nulo.")
    private Long oldAssignmentId;

    @NotNull(message = "El ID de la nueva asignación no puede ser nulo.")
    private Long newAssignmentId;

    @NotBlank(message = "La razón de la transferencia no puede estar vacía.")
    @Size(max = 255, message = "La razón de la transferencia no puede exceder los 255 caracteres.")
    private String transferReason;

    private List<String> documents; // Opcional
}