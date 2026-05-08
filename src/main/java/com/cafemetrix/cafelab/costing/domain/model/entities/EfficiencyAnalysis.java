package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class EfficiencyAnalysis extends AuditableModel {

    @Column(nullable = false)
    private Long lotPerformanceId;

    @Column(nullable = false)
    private Double efficiencyScore;

    @Column(nullable = false)
    private String performanceCategory;

    protected EfficiencyAnalysis() {}

    public EfficiencyAnalysis(Long lotPerformanceId, Double yieldPercentage, Double productivityPerHour) {
        this.lotPerformanceId = lotPerformanceId;
        this.efficiencyScore = calculateScore(yieldPercentage, productivityPerHour);
        this.performanceCategory = resolveCategory(this.efficiencyScore);
    }

    public Long getLotPerformanceId() { return lotPerformanceId; }
    public Double getEfficiencyScore() { return efficiencyScore; }
    public String getPerformanceCategory() { return performanceCategory; }

    private static Double calculateScore(Double yieldPercentage, Double productivityPerHour) {
        double normalizedProductivity = Math.min((productivityPerHour / 150.0) * 100.0, 100.0);
        return Math.round((yieldPercentage * 0.7 + normalizedProductivity * 0.3) * 100.0) / 100.0;
    }

    private static String resolveCategory(Double score) {
        if (score >= 85) return "EXCELLENT";
        if (score >= 70) return "GOOD";
        if (score >= 55) return "AVERAGE";
        return "POOR";
    }
}
