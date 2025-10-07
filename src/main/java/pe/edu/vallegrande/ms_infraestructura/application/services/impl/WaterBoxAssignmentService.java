package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterBoxAssignmentService implements IWaterBoxAssignmentService {

    private final WaterBoxAssignmentRepository waterBoxAssignmentRepository;
    private final WaterBoxRepository waterBoxRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WaterBoxAssignmentResponse> getAllActive() {
        return waterBoxAssignmentRepository.findByStatus(Status.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterBoxAssignmentResponse> getAllInactive() {
        return waterBoxAssignmentRepository.findByStatus(Status.INACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WaterBoxAssignmentResponse getById(Long id) {
        WaterBoxAssignment assignment = waterBoxAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada."));
        return toResponse(assignment);
    }

    @Override
    @Transactional
    public WaterBoxAssignmentResponse save(WaterBoxAssignmentRequest request) {
        // Validar si la WaterBox existe y está activa
        WaterBox waterBox = waterBoxRepository.findById(request.getWaterBoxId())
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada."));

        if (waterBox.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("No se puede asignar a una WaterBox inactiva.");
        }

        // Si la WaterBox ya tiene una asignación actual, desactivarla.
        //if (waterBox.getCurrentAssignmentId() != null) {
         //   waterBoxAssignmentRepository.findById(waterBox.getCurrentAssignmentId()).ifPresent(currentAssignment -> {
           //     currentAssignment.setStatus(Status.INACTIVE);
         //       currentAssignment.setEndDate(LocalDateTime.now());
        //        waterBoxAssignmentRepository.save(currentAssignment);
        //    });
       // }

        WaterBoxAssignment assignment = toEntity(request);
        assignment.setStatus(Status.ACTIVE);
        assignment.setCreatedAt(LocalDateTime.now());

        WaterBoxAssignment savedAssignment = waterBoxAssignmentRepository.save(assignment);

        // Actualizar la WaterBox con la nueva asignación actual
        waterBox.setCurrentAssignmentId(savedAssignment.getId());
        waterBoxRepository.save(waterBox);

        return toResponse(savedAssignment);
    }

    @Override
    @Transactional
    public WaterBoxAssignmentResponse update(Long id, WaterBoxAssignmentRequest request) {
        WaterBoxAssignment existingAssignment = waterBoxAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para actualizar."));

        // Asegurarse de que la WaterBox referenciada exista
        waterBoxRepository.findById(request.getWaterBoxId())
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada."));

        existingAssignment.setWaterBoxId(request.getWaterBoxId());
        existingAssignment.setUserId(request.getUserId());
        existingAssignment.setStartDate(request.getStartDate());
        existingAssignment.setMonthlyFee(request.getMonthlyFee());
        // El status y created_at no se actualizan directamente en este método.

        WaterBoxAssignment updatedAssignment = waterBoxAssignmentRepository.save(existingAssignment);
        return toResponse(updatedAssignment);
    }

    @Override
    @Transactional
    public void delete(Long id) { // Soft delete
        WaterBoxAssignment assignment = waterBoxAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para eliminación."));

        if (assignment.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("WaterBoxAssignment con ID " + id + " ya está inactiva.");
        }

        // Si esta asignación es la `current_assignment_id` de una WaterBox, desvincularla
        waterBoxRepository.findByCurrentAssignmentId(assignment.getId()).ifPresent(waterBox -> {
            waterBox.setCurrentAssignmentId(null);
            waterBoxRepository.save(waterBox);
        });

        assignment.setStatus(Status.INACTIVE);
        assignment.setEndDate(LocalDateTime.now());
        waterBoxAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public WaterBoxAssignmentResponse restore(Long id) { // Restore soft deleted
        WaterBoxAssignment assignment = waterBoxAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para restauración."));

        if (assignment.getStatus().equals(Status.ACTIVE)) {
            throw new BadRequestException("WaterBoxAssignment con ID " + id + " ya está activa.");
        }

        assignment.setStatus(Status.ACTIVE);
        assignment.setEndDate(null); // Borra la fecha de fin al restaurar
        WaterBoxAssignment restoredAssignment = waterBoxAssignmentRepository.save(assignment);

        // Si se restaura una asignación, y su WaterBox está activa, debería ser la asignación actual
        // No forzamos que sea la current_assignment_id aquí, se podría manejar con un caso de uso específico.
        // Por ahora, solo restauramos la asignación en sí.
        // Si quieres que se convierta en la current_assignment_id de su WaterBox,
        // deberías agregar lógica para eso (ej. verificar si la WaterBox tiene otra asignación activa).
        // Por simplicidad, esta lógica no lo hace automáticamente a menos que no haya otra activa.
        waterBoxRepository.findById(restoredAssignment.getWaterBoxId()).ifPresent(waterBox -> {
            // Solo actualiza current_assignment_id si la WaterBox no tiene otra asignación activa
            if (waterBox.getCurrentAssignmentId() == null || waterBoxAssignmentRepository.findById(waterBox.getCurrentAssignmentId())
                    .map(a -> a.getStatus().equals(Status.INACTIVE)).orElse(true)) {
                waterBox.setCurrentAssignmentId(restoredAssignment.getId());
                waterBoxRepository.save(waterBox);
            }
        });


        return toResponse(restoredAssignment);
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