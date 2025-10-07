package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxService;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.BadRequestException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.NotFoundException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.service.ReactiveJwtService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaterBoxService implements IWaterBoxService {

    private final WaterBoxRepository waterBoxRepository;
    private final ReactiveJwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxResponse> getAllActive() {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando cajas de agua activas", userInfo.getUsername()))
                .flatMapMany(userInfo -> waterBoxRepository.findByStatus(Status.ACTIVE))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Caja de agua activa encontrada: {}", response.getBoxCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxResponse> getAllInactive() {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando cajas de agua inactivas", userInfo.getUsername()))
                .flatMapMany(userInfo -> waterBoxRepository.findByStatus(Status.INACTIVE))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Caja de agua inactiva encontrada: {}", response.getBoxCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxResponse> getById(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} consultando caja de agua con ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + id + " no encontrada.")))
                .map(this::toResponse)
                .doOnNext(response -> log.debug("Caja de agua encontrada: {}", response.getBoxCode()));
    }

    @Override
    @Transactional
    public Mono<WaterBoxResponse> save(WaterBoxRequest request) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} creando nueva caja de agua: {}", userInfo.getUsername(), request.getBoxCode()))
                .map(userInfo -> {
                    WaterBox waterBox = toEntity(request);
                    waterBox.setStatus(Status.ACTIVE);
                    waterBox.setCreatedAt(LocalDateTime.now());
                    return waterBox;
                })
                .flatMap(waterBoxRepository::save)
                .map(this::toResponse)
                .doOnNext(response -> log.info("Caja de agua creada exitosamente: {}", response.getBoxCode()));
    }

    @Override
    @Transactional
    public Mono<WaterBoxResponse> update(Long id, WaterBoxRequest request) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} actualizando caja de agua ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + id + " no encontrada para actualizar.")))
                .map(existingWaterBox -> {
                    existingWaterBox.setOrganizationId(request.getOrganizationId());
                    existingWaterBox.setBoxCode(request.getBoxCode());
                    existingWaterBox.setBoxType(request.getBoxType());
                    existingWaterBox.setInstallationDate(request.getInstallationDate());
                    return existingWaterBox;
                })
                .flatMap(waterBoxRepository::save)
                .map(this::toResponse)
                .doOnNext(response -> log.info("Caja de agua actualizada exitosamente: {}", response.getBoxCode()));
    }

    @Override
    @Transactional
    public Mono<Void> delete(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} eliminando caja de agua ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + id + " no encontrada para eliminación.")))
                .flatMap(waterBox -> {
                    if (waterBox.getStatus().equals(Status.INACTIVE)) {
                        return Mono.error(new BadRequestException("WaterBox con ID " + id + " ya está inactiva."));
                    }
                    
                    if (waterBox.getCurrentAssignmentId() != null) {
                        return Mono.error(new BadRequestException("WaterBox con ID " + id + " tiene una asignación activa. Desactive la asignación primero."));
                    }
                    
                    waterBox.setStatus(Status.INACTIVE);
                    return waterBoxRepository.save(waterBox);
                })
                .then()
                .doOnSuccess(unused -> log.info("Caja de agua ID: {} eliminada exitosamente", id));
    }

    @Override
    @Transactional
    public Mono<WaterBoxResponse> restore(Long id) {
        return jwtService.getCurrentUserInfo()
                .doOnNext(userInfo -> log.info("Usuario {} restaurando caja de agua ID: {}", userInfo.getUsername(), id))
                .flatMap(userInfo -> waterBoxRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + id + " no encontrada para restauración.")))
                .flatMap(waterBox -> {
                    if (waterBox.getStatus().equals(Status.ACTIVE)) {
                        return Mono.error(new BadRequestException("WaterBox con ID " + id + " ya está activa."));
                    }
                    
                    waterBox.setStatus(Status.ACTIVE);
                    return waterBoxRepository.save(waterBox);
                })
                .map(this::toResponse)
                .doOnNext(response -> log.info("Caja de agua restaurada exitosamente: {}", response.getBoxCode()));
    }

    private WaterBox toEntity(WaterBoxRequest request) {
        return WaterBox.builder()
                .organizationId(request.getOrganizationId())
                .boxCode(request.getBoxCode())
                .boxType(request.getBoxType())
                .installationDate(request.getInstallationDate())
                .currentAssignmentId(request.getCurrentAssignmentId())
                .build();
    }

    private WaterBoxResponse toResponse(WaterBox waterBox) {
        return WaterBoxResponse.builder()
                .id(waterBox.getId())
                .organizationId(waterBox.getOrganizationId())
                .boxCode(waterBox.getBoxCode())
                .boxType(waterBox.getBoxType())
                .installationDate(waterBox.getInstallationDate())
                .currentAssignmentId(waterBox.getCurrentAssignmentId())
                .status(waterBox.getStatus())
                .createdAt(waterBox.getCreatedAt())
                .build();
    }
}