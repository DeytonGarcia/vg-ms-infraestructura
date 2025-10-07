package pe.edu.vallegrande.ms_infraestructura.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxTransfer;

@Repository
public interface WaterBoxTransferRepository extends JpaRepository<WaterBoxTransfer, Long> {
    // Los métodos CRUD básicos ya están provistos por JpaRepository
}