package pe.edu.vallegrande.ms_infraestructura.application.services;

import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxAssignmentRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;

import java.util.List;

public interface IWaterBoxAssignmentService {
    List<WaterBoxAssignmentResponse> getAllActive();
    List<WaterBoxAssignmentResponse> getAllInactive();
    WaterBoxAssignmentResponse getById(Long id);
    WaterBoxAssignmentResponse save(WaterBoxAssignmentRequest request);
    WaterBoxAssignmentResponse update(Long id, WaterBoxAssignmentRequest request);
    void delete(Long id); // Soft delete
    WaterBoxAssignmentResponse restore(Long id); // Restore soft deleted
}