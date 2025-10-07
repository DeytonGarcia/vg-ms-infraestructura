package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;
import reactor.core.publisher.Flux;

@Repository
public interface WaterBoxAssignmentRepository extends R2dbcRepository<WaterBoxAssignment, Long> {
    Flux<WaterBoxAssignment> findByStatus(Status status);
}