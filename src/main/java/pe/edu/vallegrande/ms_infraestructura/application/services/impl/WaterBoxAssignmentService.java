package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxAssignmentRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.BadRequestException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.NotFoundException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxAssignmentRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.service.ReactiveJwtService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaterBoxAssignmentService implements IWaterBoxAssignmentService {

    private final WaterBoxAssignmentRepository waterBoxAssignmentRepository;
    private final WaterBoxRepository waterBoxRepository;
    private final ReactiveJwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxAssignmentResponse> getAllActive() {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando asignaciones activas", userInfo.getUsername()))
                .flatMapMany(userInfo -> waterBoxAssignmentRepository.findByStatus(Status.ACTIVE))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Asignación activa encontrada: {}", response.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxAssignmentResponse> getAllInactive() {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando asignaciones inactivas", userInfo.getUsername()))
                .flatMapMany(userInfo -> waterBoxAssignmentRepository.findByStatus(Status.INACTIVE))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Asignación inactiva encontrada: {}", response.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxAssignmentResponse> getById(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando asignación con ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxAssignmentRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada.")))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Asignación encontrada: {}", response.getId()));
    }

    @Override
    @Transactional
    public Mono<WaterBoxAssignmentResponse> save(WaterBoxAssignmentRequest request) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} creando nueva asignación para WaterBox ID: {}", userInfo.getUsername(), request.getWaterBoxId()))
                .flatMap(userInfo -> waterBoxRepository.findById(request.getWaterBoxId()))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada.")))
                .flatMap(waterBox -> {
                    if (waterBox.getStatus().equals(Status.INACTIVE)) {
                        return Mono.error(new BadRequestException("No se puede asignar a una WaterBox inactiva."));
                    }
                    
                    WaterBoxAssignment assignment = toEntity(request);
                    assignment.setStatus(Status.ACTIVE);
                    assignment.setCreatedAt(LocalDateTime.now());
                    
                    return waterBoxAssignmentRepository.save(assignment)
                            .flatMap(savedAssignment -> {
                                // Actualizar la WaterBox con la nueva asignación actual
                                waterBox.setCurrentAssignmentId(savedAssignment.getId());
                                return waterBoxRepository.save(waterBox)
                                        .thenReturn(savedAssignment);
                            });
                })
                .map(this::toResponse)
                .doOnNext(response -> log.info("Asignación creada exitosamente: {}", response.getId()));
    }

    @Override
    @Transactional
    public Mono<WaterBoxAssignmentResponse> update(Long id, WaterBoxAssignmentRequest request) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} actualizando asignación ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxAssignmentRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para actualizar.")))
                .flatMap(existingAssignment -> 
                    waterBoxRepository.findById(request.getWaterBoxId())
                            .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada.")))
                            .map(waterBox -> {
                                existingAssignment.setWaterBoxId(request.getWaterBoxId());
                                existingAssignment.setUserId(request.getUserId());
                                existingAssignment.setStartDate(request.getStartDate());
                                existingAssignment.setMonthlyFee(request.getMonthlyFee());
                                return existingAssignment;
                            })
                )
                .flatMap(waterBoxAssignmentRepository::save)
                .map(this::toResponse)
                .doOnNext(response -> log.info("Asignación actualizada exitosamente: {}", response.getId()));
    }

    @Override
    @Transactional
    public Mono<Void> delete(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} eliminando asignación ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxAssignmentRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para eliminación.")))
                .flatMap(assignment -> {
                    if (assignment.getStatus().equals(Status.INACTIVE)) {
                        return Mono.error(new BadRequestException("WaterBoxAssignment con ID " + id + " ya está inactiva."));
                    }
                    
                    // Si esta asignación es la current_assignment_id de una WaterBox, desvincularla
                    return waterBoxRepository.findByCurrentAssignmentId(assignment.getId())
                            .flatMap(waterBox -> {
                                waterBox.setCurrentAssignmentId(null);
                                return waterBoxRepository.save(waterBox);
                            })
                            .then(Mono.fromCallable(() -> {
                                assignment.setStatus(Status.INACTIVE);
                                assignment.setEndDate(LocalDateTime.now());
                                return assignment;
                            }))
                            .flatMap(waterBoxAssignmentRepository::save);
                })
                .then()
                .doOnSuccess(unused -> log.info("Asignación ID: {} eliminada exitosamente", id));
    }

    @Override
    @Transactional
    public Mono<WaterBoxAssignmentResponse> restore(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} restaurando asignación ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxAssignmentRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para restauración.")))
                .flatMap(assignment -> {
                    if (assignment.getStatus().equals(Status.ACTIVE)) {
                        return Mono.error(new BadRequestException("WaterBoxAssignment con ID " + id + " ya está activa."));
                    }
                    
                    assignment.setStatus(Status.ACTIVE);
                    assignment.setEndDate(null);
                    
                    return waterBoxAssignmentRepository.save(assignment)
                            .flatMap(restoredAssignment -> 
                                waterBoxRepository.findById(restoredAssignment.getWaterBoxId())
                                        .flatMap(waterBox -> {
                                            // Solo actualiza current_assignment_id si la WaterBox no tiene otra asignación activa
                                            if (waterBox.getCurrentAssignmentId() == null) {
                                                waterBox.setCurrentAssignmentId(restoredAssignment.getId());
                                                return waterBoxRepository.save(waterBox)
                                                        .thenReturn(restoredAssignment);
                                            }
                                            return Mono.just(restoredAssignment);
                                        })
                                        .switchIfEmpty(Mono.just(restoredAssignment))
                            );
                })
                .map(this::toResponse)
                .doOnNext(response -> log.info("Asignación restaurada exitosamente: {}", response.getId()));
    }

    private WaterBoxAssignment toEntity(WaterBoxAssignmentRequest request) {
        return WaterBoxAssignment.builder()
                .waterBoxId(request.getWaterBoxId())
                .userId(request.getUserId())
                .startDate(request.getStartDate())
                .monthlyFee(request.getMonthlyFee())
                .build();
    }

    private WaterBoxAssignmentResponse toResponse(WaterBoxAssignment assignment) {
        return WaterBoxAssignmentResponse.builder()
                .id(assignment.getId())
                .waterBoxId(assignment.getWaterBoxId())
                .userId(assignment.getUserId())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .monthlyFee(assignment.getMonthlyFee())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .transferId(assignment.getTransferId())
                .build();
    }
}