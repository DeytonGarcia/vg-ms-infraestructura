package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest.client;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxTransferService;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientRest {

    private final IWaterBoxService waterBoxService;
    private final IWaterBoxAssignmentService waterBoxAssignmentService;
    private final IWaterBoxTransferService waterBoxTransferService;

    // ===============================
    // GESTIÓN DE WATER BOXES
    // ===============================

    @GetMapping("/water-boxes/active")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Flux<WaterBoxResponse> getAllActiveWaterBoxes() {
        return waterBoxService.getAllActive();
    }

    @GetMapping("/water-boxes/inactive")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Flux<WaterBoxResponse> getAllInactiveWaterBoxes() {
        return waterBoxService.getAllInactive();
    }

    @GetMapping("/water-boxes/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Mono<WaterBoxResponse> getWaterBoxById(@PathVariable Long id) {
        return waterBoxService.getById(id);
    }

    // ===============================
    // GESTIÓN DE WATER BOX ASSIGNMENTS
    // ===============================

    @GetMapping("/water-box-assignments/active")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Flux<WaterBoxAssignmentResponse> getAllActiveAssignments() {
        return waterBoxAssignmentService.getAllActive();
    }

    @GetMapping("/water-box-assignments/inactive")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Flux<WaterBoxAssignmentResponse> getAllInactiveAssignments() {
        return waterBoxAssignmentService.getAllInactive();
    }

    @GetMapping("/water-box-assignments/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Mono<WaterBoxAssignmentResponse> getAssignmentById(@PathVariable Long id) {
        return waterBoxAssignmentService.getById(id);
    }

    // ===============================
    // GESTIÓN DE WATER BOX TRANSFERS
    // ===============================

    @GetMapping("/water-box-transfers")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Flux<WaterBoxTransferResponse> getAllTransfers() {
        return waterBoxTransferService.getAll();
    }

    @GetMapping("/water-box-transfers/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public Mono<WaterBoxTransferResponse> getTransferById(@PathVariable Long id) {
        return waterBoxTransferService.getById(id);
    }
}