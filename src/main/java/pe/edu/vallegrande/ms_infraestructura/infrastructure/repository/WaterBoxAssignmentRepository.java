package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;

import java.util.List;

@Repository
public interface WaterBoxAssignmentRepository extends JpaRepository<WaterBoxAssignment, Long> {
    List<WaterBoxAssignment> findByStatus(Status status);
}