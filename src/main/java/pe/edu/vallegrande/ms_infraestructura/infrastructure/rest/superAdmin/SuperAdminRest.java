package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest.superAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxTransferService;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxAssignmentRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxTransferRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
public class SuperAdminRest {

    private final IWaterBoxService waterBoxService;
    private final IWaterBoxAssignmentService waterBoxAssignmentService;
    private final IWaterBoxTransferService waterBoxTransferService;

    // ===============================
    // GESTIÓN DE WATER BOXES
    // ===============================

    @PostMapping("/water-boxes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<WaterBoxResponse> createWaterBox(@Valid @RequestBody WaterBoxRequest request) {
        return waterBoxService.save(request);
    }

    @PutMapping("/water-boxes/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<WaterBoxResponse> updateWaterBox(@PathVariable Long id, @Valid @RequestBody WaterBoxRequest request) {
        return waterBoxService.update(id, request);
    }

    @DeleteMapping("/water-boxes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<Void> deleteWaterBox(@PathVariable Long id) {
        return waterBoxService.delete(id);
    }

    // ===============================
    // GESTIÓN DE WATER BOX ASSIGNMENTS
    // ===============================

    @PostMapping("/water-box-assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<WaterBoxAssignmentResponse> createAssignment(@Valid @RequestBody WaterBoxAssignmentRequest request) {
        return waterBoxAssignmentService.save(request);
    }

    @PutMapping("/water-box-assignments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<WaterBoxAssignmentResponse> updateAssignment(@PathVariable Long id, @Valid @RequestBody WaterBoxAssignmentRequest request) {
        return waterBoxAssignmentService.update(id, request);
    }

    @DeleteMapping("/water-box-assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<Void> deleteAssignment(@PathVariable Long id) {
        return waterBoxAssignmentService.delete(id);
    }

    // ===============================
    // GESTIÓN DE WATER BOX TRANSFERS
    // ===============================

    @PostMapping("/water-box-transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<WaterBoxTransferResponse> createTransfer(@Valid @RequestBody WaterBoxTransferRequest request) {
        return waterBoxTransferService.save(request);
    }
}
