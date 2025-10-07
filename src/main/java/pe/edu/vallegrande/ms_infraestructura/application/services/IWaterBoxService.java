package pe.edu.vallegrande.ms_infraestructura.application.services;

import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;

import java.util.List;

public interface IWaterBoxService {
    List<WaterBoxResponse> getAllActive();
    List<WaterBoxResponse> getAllInactive();
    WaterBoxResponse getById(Long id);
    WaterBoxResponse save(WaterBoxRequest request);
    WaterBoxResponse update(Long id, WaterBoxRequest request);
    void delete(Long id); // Soft delete
    WaterBoxResponse restore(Long id); // Restore soft deleted
}