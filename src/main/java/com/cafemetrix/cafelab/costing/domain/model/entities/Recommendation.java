package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.AddRecommendationCommand;
import com.cafemetrix.cafelab.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Recomendación textual asociada a un Batch (PDF 4.1.4.3.4). Permite registrar
 * observaciones cualitativas que complementan los indicadores financieros.
 */
@Entity
@Table(name = "recommendations")
public class Recommendation extends AuditableModel {

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    /**
     * Texto libre. Se mapea como VARCHAR(2000) (sin {@code columnDefinition = "TEXT"})
     * para mantener portabilidad: TEXT está deprecado en SQL Server y el monolito usa
     * Azure SQL. 2000 caracteres alcanzan para recomendaciones cualitativas.
     */
    @Column(name = "recommendation_text", nullable = false, length = 2000)
    private String recommendationText;

    protected Recommendation() {}

    public Recommendation(Long batchId, AddRecommendationCommand command) {
        if (command.recommendationText() == null || command.recommendationText().isBlank()) {
            throw new IllegalArgumentException("recommendationText is required");
        }
        this.batchId = batchId;
        this.recommendationText = command.recommendationText().trim();
    }

    public Long getBatchId() { return batchId; }
    public String getRecommendationText() { return recommendationText; }
}
