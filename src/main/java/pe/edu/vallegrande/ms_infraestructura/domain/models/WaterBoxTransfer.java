package pe.edu.vallegrande.ms_infraestructura.domain.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Table("water_box_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBoxTransfer {
    @Id
    private Long id;

    @Column("water_box_id")
    private Long waterBoxId;

    @Column("old_assignment_id")
    private Long oldAssignmentId;

    @Column("new_assignment_id")
    private Long newAssignmentId;

    @Column("transfer_reason")
    private String transferReason;

    @Column("documents")
    private String documentsJson; // Almacenaremos como JSON string

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    // Métodos helper para manejar la lista de documentos
    public List<String> getDocuments() {
        if (documentsJson == null || documentsJson.isEmpty()) {
            return List.of();
        }
        try {
            return List.of(documentsJson.split(","));
        } catch (Exception e) {
            return List.of();
        }
    }

    public void setDocuments(List<String> documents) {
        if (documents == null || documents.isEmpty()) {
            this.documentsJson = null;
        } else {
            this.documentsJson = String.join(",", documents);
        }
    }
}