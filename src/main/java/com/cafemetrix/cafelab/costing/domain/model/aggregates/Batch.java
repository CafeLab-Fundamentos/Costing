package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.CreateBatchCommand;
import com.cafemetrix.cafelab.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Agregado raíz del bounded context Costing (PDF 4.1.4.3.4 / 4.1.5.4).
 * Un Batch agrupa los costos directos, indirectos, su resumen, los indicadores
 * financieros calculados y las recomendaciones del usuario. {@code userId} se
 * propaga desde el JWT para acotar la visibilidad por dueño.
 */
@Entity
@Table(name = "batchs")
public class Batch extends AuditableAbstractAggregateRoot<Batch> {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "batch_name", nullable = false, length = 160)
    private String batchName;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    protected Batch() {}

    public Batch(CreateBatchCommand command) {
        if (command.batchName() == null || command.batchName().isBlank()) {
            throw new IllegalArgumentException("batchName is required");
        }
        this.userId = command.userId();
        this.batchName = command.batchName().trim();
        this.registrationDate = command.registrationDate() != null
                ? command.registrationDate()
                : LocalDate.now();
    }

    public Long getUserId() { return userId; }
    public String getBatchName() { return batchName; }
    public LocalDate getRegistrationDate() { return registrationDate; }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("batchName is required");
        }
        this.batchName = newName.trim();
    }
}
