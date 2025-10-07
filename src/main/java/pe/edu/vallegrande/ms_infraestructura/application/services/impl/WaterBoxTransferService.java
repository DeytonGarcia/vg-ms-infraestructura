package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterBoxTransferService implements IWaterBoxTransferService {

    private final WaterBoxTransferRepository waterBoxTransferRepository;
    private final WaterBoxAssignmentRepository waterBoxAssignmentRepository;
    private final WaterBoxRepository waterBoxRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WaterBoxTransferResponse> getAll() {
        return waterBoxTransferRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WaterBoxTransferResponse getById(Long id) {
        WaterBoxTransfer transfer = waterBoxTransferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxTransfer con ID " + id + " no encontrada."));
        return toResponse(transfer);
    }

    @Override
    @Transactional
    public WaterBoxTransferResponse save(WaterBoxTransferRequest request) {
        // 1. Validar que la caja de agua exista y esté activa
        WaterBox waterBox = waterBoxRepository.findById(request.getWaterBoxId())
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada."));

        if (waterBox.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("No se puede transferir una WaterBox inactiva.");
        }

        // 2. Validar y procesar la asignación antigua (old_assignment_id)
        WaterBoxAssignment oldAssignment = waterBoxAssignmentRepository.findById(request.getOldAssignmentId())
                .orElseThrow(() -> new NotFoundException("Asignación antigua con ID " + request.getOldAssignmentId() + " no encontrada."));

        if (!oldAssignment.getWaterBoxId().equals(waterBox.getId())) {
            throw new BadRequestException("La asignación antigua no pertenece a la WaterBox especificada.");
        }
        if (oldAssignment.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("La asignación antigua ya está inactiva. No se puede iniciar transferencia desde una asignación inactiva.");
        }
        // Asegurarse de que la oldAssignmentId sea la asignación actualmente activa para esa caja de agua
        if (waterBox.getCurrentAssignmentId() == null || !waterBox.getCurrentAssignmentId().equals(oldAssignment.getId())) {
            throw new BadRequestException("La asignación antigua proporcionada no es la asignación actual activa para esta WaterBox.");
        }


        // 3. Validar y procesar la nueva asignación (new_assignment_id)
        WaterBoxAssignment newAssignment = waterBoxAssignmentRepository.findById(request.getNewAssignmentId())
                .orElseThrow(() -> new NotFoundException("Nueva asignación con ID " + request.getNewAssignmentId() + " no encontrada."));

        if (!newAssignment.getWaterBoxId().equals(waterBox.getId())) {
            throw new BadRequestException("La nueva asignación no pertenece a la WaterBox especificada.");
        }
        if (newAssignment.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("La nueva asignación está inactiva. Debería estar activa para una nueva transferencia.");
        }
        if (newAssignment.getId().equals(oldAssignment.getId())) {
            throw new BadRequestException("La asignación antigua y la nueva no pueden ser la misma.");
        }

        // 4. Crear la transferencia
        WaterBoxTransfer transfer = toEntity(request);
        transfer.setCreatedAt(LocalDateTime.now());
        WaterBoxTransfer savedTransfer = waterBoxTransferRepository.save(transfer);

        // 5. Actualizar la asignación antigua: desactivarla y asignarle la fecha de fin y el ID de transferencia
        oldAssignment.setStatus(Status.INACTIVE);
        oldAssignment.setEndDate(LocalDateTime.now());
        oldAssignment.setTransferId(savedTransfer.getId());
        waterBoxAssignmentRepository.save(oldAssignment);

        // 6. Actualizar la caja de agua con la nueva asignación actual
        waterBox.setCurrentAssignmentId(newAssignment.getId());
        waterBoxRepository.save(waterBox);

        return toResponse(savedTransfer);
    }

    private WaterBoxTransfer toEntity(WaterBoxTransferRequest request) {
        return WaterBoxTransfer.builder()
                .waterBoxId(request.getWaterBoxId())
                .oldAssignmentId(request.getOldAssignmentId())
                .newAssignmentId(request.getNewAssignmentId())
                .transferReason(request.getTransferReason())
                .documents(request.getDocuments())
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