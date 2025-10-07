-- Crear tablas para el microservicio de infraestructura
-- Este script es compatible con PostgreSQL

-- Tabla de cajas de agua
CREATE TABLE IF NOT EXISTS water_boxes (
    id BIGSERIAL PRIMARY KEY,
    organization_id VARCHAR(255) NOT NULL,
    box_code VARCHAR(50) NOT NULL UNIQUE,
    box_type VARCHAR(20) NOT NULL CHECK (box_type IN ('CAÑO', 'BOMBA', 'OTRO')),
    installation_date DATE NOT NULL,
    current_assignment_id BIGINT,
    status VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de asignaciones de cajas de agua
CREATE TABLE IF NOT EXISTS water_box_assignments (
    id BIGSERIAL PRIMARY KEY,
    water_box_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    monthly_fee DECIMAL(10,2) NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transfer_id BIGINT,
    FOREIGN KEY (water_box_id) REFERENCES water_boxes(id)
);

-- Tabla de transferencias de cajas de agua
CREATE TABLE IF NOT EXISTS water_box_transfers (
    id BIGSERIAL PRIMARY KEY,
    water_box_id BIGINT NOT NULL,
    old_assignment_id BIGINT NOT NULL,
    new_assignment_id BIGINT NOT NULL,
    transfer_reason VARCHAR(255) NOT NULL,
    documents TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (water_box_id) REFERENCES water_boxes(id),
    FOREIGN KEY (old_assignment_id) REFERENCES water_box_assignments(id),
    FOREIGN KEY (new_assignment_id) REFERENCES water_box_assignments(id)
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_water_boxes_status ON water_boxes(status);
CREATE INDEX IF NOT EXISTS idx_water_boxes_organization ON water_boxes(organization_id);
CREATE INDEX IF NOT EXISTS idx_water_boxes_code ON water_boxes(box_code);

CREATE INDEX IF NOT EXISTS idx_assignments_status ON water_box_assignments(status);
CREATE INDEX IF NOT EXISTS idx_assignments_water_box ON water_box_assignments(water_box_id);
CREATE INDEX IF NOT EXISTS idx_assignments_user ON water_box_assignments(user_id);

CREATE INDEX IF NOT EXISTS idx_transfers_water_box ON water_box_transfers(water_box_id);
CREATE INDEX IF NOT EXISTS idx_transfers_old_assignment ON water_box_transfers(old_assignment_id);
CREATE INDEX IF NOT EXISTS idx_transfers_new_assignment ON water_box_transfers(new_assignment_id);

-- Comentarios para documentación
COMMENT ON TABLE water_boxes IS 'Tabla que almacena información de las cajas de agua del sistema JASS';
COMMENT ON TABLE water_box_assignments IS 'Tabla que almacena las asignaciones de cajas de agua a usuarios';
COMMENT ON TABLE water_box_transfers IS 'Tabla que almacena el historial de transferencias de cajas de agua entre usuarios';

COMMENT ON COLUMN water_boxes.organization_id IS 'ID de la organización JASS a la que pertenece la caja de agua';
COMMENT ON COLUMN water_boxes.box_code IS 'Código único identificador de la caja de agua';
COMMENT ON COLUMN water_boxes.box_type IS 'Tipo de caja de agua: CAÑO, BOMBA, OTRO';
COMMENT ON COLUMN water_boxes.current_assignment_id IS 'ID de la asignación actual activa (si existe)';

COMMENT ON COLUMN water_box_assignments.user_id IS 'ID del usuario al que está asignada la caja de agua';
COMMENT ON COLUMN water_box_assignments.monthly_fee IS 'Tarifa mensual en soles para esta asignación';
COMMENT ON COLUMN water_box_assignments.transfer_id IS 'ID de la transferencia que inactivó esta asignación (si aplica)';

COMMENT ON COLUMN water_box_transfers.transfer_reason IS 'Razón o motivo de la transferencia';
COMMENT ON COLUMN water_box_transfers.documents IS 'Documentos adjuntos relacionados con la transferencia (almacenados como texto separado por comas)';