package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxTransferService;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxTransfer;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxTransferRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.BadRequestException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.NotFoundException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxAssignmentRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxTransferRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.service.ReactiveJwtService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaterBoxTransferService implements IWaterBoxTransferService {

    private final WaterBoxTransferRepository waterBoxTransferRepository;
    private final WaterBoxAssignmentRepository waterBoxAssignmentRepository;
    private final WaterBoxRepository waterBoxRepository;
    private final ReactiveJwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxTransferResponse> getAll() {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando todas las transferencias", userInfo.getUsername()))
                .flatMapMany(userInfo -> waterBoxTransferRepository.findAll())
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Transferencia encontrada: {}", response.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxTransferResponse> getById(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando transferencia con ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxTransferRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxTransfer con ID " + id + " no encontrada.")))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Transferencia encontrada: {}", response.getId()));
    }

    @Override
    @Transactional
    public Mono<WaterBoxTransferResponse> save(WaterBoxTransferRequest request) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} creando transferencia para WaterBox ID: {}", userInfo.getUsername(), request.getWaterBoxId()))
                .flatMap(userInfo -> 
                    // 1. Validar que la caja de agua exista y esté activa
                    waterBoxRepository.findById(request.getWaterBoxId())
                            .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada.")))
                            .flatMap(waterBox -> {
                                if (waterBox.getStatus().equals(Status.INACTIVE)) {
                                    return Mono.error(new BadRequestException("No se puede transferir una WaterBox inactiva."));
                                }
                                
                                // 2. Validar asignación antigua
                                return waterBoxAssignmentRepository.findById(request.getOldAssignmentId())
                                        .switchIfEmpty(Mono.error(new NotFoundException("Asignación antigua con ID " + request.getOldAssignmentId() + " no encontrada.")))
                                        .flatMap(oldAssignment -> {
                                            if (!oldAssignment.getWaterBoxId().equals(waterBox.getId())) {
                                                return Mono.error(new BadRequestException("La asignación antigua no pertenece a la WaterBox especificada."));
                                            }
                                            if (oldAssignment.getStatus().equals(Status.INACTIVE)) {
                                                return Mono.error(new BadRequestException("La asignación antigua ya está inactiva."));
                                            }
                                            if (waterBox.getCurrentAssignmentId() == null || !waterBox.getCurrentAssignmentId().equals(oldAssignment.getId())) {
                                                return Mono.error(new BadRequestException("La asignación antigua proporcionada no es la asignación actual activa."));
                                            }
                                            
                                            // 3. Validar nueva asignación
                                            return waterBoxAssignmentRepository.findById(request.getNewAssignmentId())
                                                    .switchIfEmpty(Mono.error(new NotFoundException("Nueva asignación con ID " + request.getNewAssignmentId() + " no encontrada.")))
                                                    .flatMap(newAssignment -> {
                                                        if (!newAssignment.getWaterBoxId().equals(waterBox.getId())) {
                                                            return Mono.error(new BadRequestException("La nueva asignación no pertenece a la WaterBox especificada."));
                                                        }
                                                        if (newAssignment.getStatus().equals(Status.INACTIVE)) {
                                                            return Mono.error(new BadRequestException("La nueva asignación está inactiva."));
                                                        }
                                                        if (newAssignment.getId().equals(oldAssignment.getId())) {
                                                            return Mono.error(new BadRequestException("La asignación antigua y la nueva no pueden ser la misma."));
                                                        }
                                                        
                                                        // 4. Crear la transferencia
                                                        WaterBoxTransfer transfer = toEntity(request);
                                                        transfer.setCreatedAt(LocalDateTime.now());
                                                        
                                                        return waterBoxTransferRepository.save(transfer)
                                                                .flatMap(savedTransfer -> {
                                                                    // 5. Actualizar asignación antigua
                                                                    oldAssignment.setStatus(Status.INACTIVE);
                                                                    oldAssignment.setEndDate(LocalDateTime.now());
                                                                    oldAssignment.setTransferId(savedTransfer.getId());
                                                                    
                                                                    return waterBoxAssignmentRepository.save(oldAssignment)
                                                                            .flatMap(updatedOldAssignment -> {
                                                                                // 6. Actualizar WaterBox con nueva asignación
                                                                                waterBox.setCurrentAssignmentId(newAssignment.getId());
                                                                                return waterBoxRepository.save(waterBox)
                                                                                        .thenReturn(savedTransfer);
                                                                            });
                                                                });
                                                    });
                                        });
                            })
                )
                .map(this::toResponse)
                .doOnNext(response -> log.info("Transferencia creada exitosamente: {}", response.getId()));
    }

    private WaterBoxTransfer toEntity(WaterBoxTransferRequest request) {
        return WaterBoxTransfer.builder()
                .waterBoxId(request.getWaterBoxId())
                .oldAssignmentId(request.getOldAssignmentId())
                .newAssignmentId(request.getNewAssignmentId())
                .transferReason(request.getTransferReason())
                .documentsJson(request.getDocuments() != null ? String.join(",", request.getDocuments()) : null)
                .build();
    }

    private WaterBoxTransferResponse toResponse(WaterBoxTransfer transfer) {
        return WaterBoxTransferResponse.builder()
                .id(transfer.getId())
                .waterBoxId(transfer.getWaterBoxId())
                .oldAssignmentId(transfer.getOldAssignmentId())
                .newAssignmentId(transfer.getNewAssignmentId())
                .transferReason(transfer.getTransferReason())
                .documents(transfer.getDocuments())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}