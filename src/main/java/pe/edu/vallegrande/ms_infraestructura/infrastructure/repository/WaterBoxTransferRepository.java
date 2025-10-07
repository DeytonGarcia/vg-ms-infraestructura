package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxTransfer;

@Repository
public interface WaterBoxTransferRepository extends R2dbcRepository<WaterBoxTransfer, Long> {
    // Los métodos CRUD básicos ya están provistos por R2dbcRepository
}