package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterBoxRepository extends JpaRepository<WaterBox, Long> {
    List<WaterBox> findByStatus(Status status);
    Optional<WaterBox> findByCurrentAssignmentId(Long currentAssignmentId);
}