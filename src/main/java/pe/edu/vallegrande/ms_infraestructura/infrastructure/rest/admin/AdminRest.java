package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest.admin;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRest {

    private final IWaterBoxService waterBoxService;
    private final IWaterBoxAssignmentService waterBoxAssignmentService;
    private final IWaterBoxTransferService waterBoxTransferService;

    // ===============================
    // GESTIÓN DE WATER BOXES
    // ===============================

    @GetMapping("/water-boxes/active")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<WaterBoxResponse> getAllActiveWaterBoxes() {
        return waterBoxService.getAllActive();
    }

    @GetMapping("/water-boxes/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<WaterBoxResponse> getAllInactiveWaterBoxes() {
        return waterBoxService.getAllInactive();
    }

    @GetMapping("/water-boxes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxResponse> getWaterBoxById(@PathVariable Long id) {
        return waterBoxService.getById(id);
    }

    @PostMapping("/water-boxes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxResponse> createWaterBox(@Valid @RequestBody WaterBoxRequest request) {
        return waterBoxService.save(request);
    }

    @PutMapping("/water-boxes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxResponse> updateWaterBox(@PathVariable Long id, @Valid @RequestBody WaterBoxRequest request) {
        return waterBoxService.update(id, request);
    }

    @DeleteMapping("/water-boxes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteWaterBox(@PathVariable Long id) {
        return waterBoxService.delete(id);
    }

    @PatchMapping("/water-boxes/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxResponse> restoreWaterBox(@PathVariable Long id) {
        return waterBoxService.restore(id);
    }

    // ===============================
    // GESTIÓN DE WATER BOX ASSIGNMENTS
    // ===============================

    @GetMapping("/water-box-assignments/active")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<WaterBoxAssignmentResponse> getAllActiveAssignments() {
        return waterBoxAssignmentService.getAllActive();
    }

    @GetMapping("/water-box-assignments/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<WaterBoxAssignmentResponse> getAllInactiveAssignments() {
        return waterBoxAssignmentService.getAllInactive();
    }

    @GetMapping("/water-box-assignments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxAssignmentResponse> getAssignmentById(@PathVariable Long id) {
        return waterBoxAssignmentService.getById(id);
    }

    @PostMapping("/water-box-assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxAssignmentResponse> createAssignment(@Valid @RequestBody WaterBoxAssignmentRequest request) {
        return waterBoxAssignmentService.save(request);
    }

    @PutMapping("/water-box-assignments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxAssignmentResponse> updateAssignment(@PathVariable Long id, @Valid @RequestBody WaterBoxAssignmentRequest request) {
        return waterBoxAssignmentService.update(id, request);
    }

    @DeleteMapping("/water-box-assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteAssignment(@PathVariable Long id) {
        return waterBoxAssignmentService.delete(id);
    }

    @PatchMapping("/water-box-assignments/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxAssignmentResponse> restoreAssignment(@PathVariable Long id) {
        return waterBoxAssignmentService.restore(id);
    }

    // ===============================
    // GESTIÓN DE WATER BOX TRANSFERS
    // ===============================

    @GetMapping("/water-box-transfers")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<WaterBoxTransferResponse> getAllTransfers() {
        return waterBoxTransferService.getAll();
    }

    @GetMapping("/water-box-transfers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxTransferResponse> getTransferById(@PathVariable Long id) {
        return waterBoxTransferService.getById(id);
    }

    @PostMapping("/water-box-transfers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<WaterBoxTransferResponse> createTransfer(@Valid @RequestBody WaterBoxTransferRequest request) {
        return waterBoxTransferService.save(request);
    }
}