package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Indicadores financieros derivados del Batch (PDF 4.1.4.3.4). Apoya la
 * "rentabilidad por lote" exigida por US11 mediante el margen objetivo del
 * usuario para sugerir el precio de venta.
 */
@Entity
@Table(name = "financial_indicators",
        uniqueConstraints = @UniqueConstraint(name = "uk_financial_indicators_batch", columnNames = {"batch_id"}))
public class FinancialIndicators extends AuditableModel {

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "cost_per_kg", nullable = false)
    private Double costPerKg;

    @Column(name = "potential_margin", nullable = false)
    private Double potentialMargin;

    @Column(name = "suggested_price", nullable = false)
    private Double suggestedPrice;

    protected FinancialIndicators() {}

    public FinancialIndicators(Long batchId, CostSummary summary, Double targetMarginPercentage) {
        this.batchId = batchId;
        recompute(summary, targetMarginPercentage);
    }

    public void recompute(CostSummary summary, Double targetMarginPercentage) {
        if (targetMarginPercentage == null || targetMarginPercentage < 0) {
            throw new IllegalArgumentException("targetMarginPercentage must be >= 0");
        }
        this.costPerKg = summary.getCostPerKg();
        this.potentialMargin = round(targetMarginPercentage);
        this.suggestedPrice = round(summary.getCostPerKg() * (1.0 + targetMarginPercentage / 100.0));
    }

    private static Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Long getBatchId() { return batchId; }
    public Double getCostPerKg() { return costPerKg; }
    public Double getPotentialMargin() { return potentialMargin; }
    public Double getSuggestedPrice() { return suggestedPrice; }
}
