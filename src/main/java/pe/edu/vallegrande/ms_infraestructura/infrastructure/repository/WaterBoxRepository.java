package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WaterBoxRepository extends R2dbcRepository<WaterBox, Long> {
    Flux<WaterBox> findByStatus(Status status);
    Mono<WaterBox> findByCurrentAssignmentId(Long currentAssignmentId);
}