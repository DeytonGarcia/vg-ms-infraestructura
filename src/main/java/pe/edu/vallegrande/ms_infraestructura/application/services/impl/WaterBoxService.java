package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterBoxService implements IWaterBoxService {

    private final WaterBoxRepository waterBoxRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WaterBoxResponse> getAllActive() {
        return waterBoxRepository.findByStatus(Status.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterBoxResponse> getAllInactive() {
        return waterBoxRepository.findByStatus(Status.INACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WaterBoxResponse getById(Long id) {
        WaterBox waterBox = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada."));
        return toResponse(waterBox);
    }

    @Override
    @Transactional
    public WaterBoxResponse save(WaterBoxRequest request) {
        // Opcional: Validar si organizationId existe en un microservicio de organizaciones
        WaterBox waterBox = toEntity(request);
        waterBox.setStatus(Status.ACTIVE);
        waterBox.setCreatedAt(LocalDateTime.now());
        WaterBox savedWaterBox = waterBoxRepository.save(waterBox);
        return toResponse(savedWaterBox);
    }

    @Override
    @Transactional
    public WaterBoxResponse update(Long id, WaterBoxRequest request) {
        WaterBox existingWaterBox = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada para actualizar."));

        existingWaterBox.setOrganizationId(request.getOrganizationId());
        existingWaterBox.setBoxCode(request.getBoxCode());
        existingWaterBox.setBoxType(request.getBoxType());
        existingWaterBox.setInstallationDate(request.getInstallationDate());
        // currentAssignmentId y status no se actualizan directamente desde aquí,
        // sino a través de operaciones de asignación y transferencia.

        WaterBox updatedWaterBox = waterBoxRepository.save(existingWaterBox);
        return toResponse(updatedWaterBox);
    }

    @Override
    @Transactional
    public void delete(Long id) { // Soft delete
        WaterBox waterBox = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada para eliminación."));

        if (waterBox.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("WaterBox con ID " + id + " ya está inactiva.");
        }

        // Si la caja de agua tiene una asignación actual, debe desvincularse.
        // La lógica de negocio aquí sería que una caja de agua inactiva NO debe tener una asignación actual.
        if (waterBox.getCurrentAssignmentId() != null) {
            throw new BadRequestException("WaterBox con ID " + id + " tiene una asignación activa. Desactive la asignación primero.");
            // O podrías poner waterBox.setCurrentAssignmentId(null); y guardar waterBox
            // Dependerá de la regla de negocio: si al borrar la caja, se desactivan automáticamente las asignaciones.
            // Por simplicidad, asumimos que no se puede eliminar si tiene asignación activa.
        }

        waterBox.setStatus(Status.INACTIVE);
        waterBoxRepository.save(waterBox);
    }

    @Override
    @Transactional
    public WaterBoxResponse restore(Long id) { // Restore soft deleted
        WaterBox waterBox = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada para restauración."));

        if (waterBox.getStatus().equals(Status.ACTIVE)) {
            throw new BadRequestException("WaterBox con ID " + id + " ya está activa.");
        }

        waterBox.setStatus(Status.ACTIVE);
        WaterBox restoredWaterBox = waterBoxRepository.save(waterBox);
        return toResponse(restoredWaterBox);
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