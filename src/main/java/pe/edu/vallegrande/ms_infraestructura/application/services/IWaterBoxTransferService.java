package pe.edu.vallegrande.ms_infraestructura.application.services;

import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxTransferRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;

import java.util.List;

public interface IWaterBoxTransferService {
    List<WaterBoxTransferResponse> getAll();
    WaterBoxTransferResponse getById(Long id);
    WaterBoxTransferResponse save(WaterBoxTransferRequest request);
}