package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Resumen agregado de los costos del Batch (PDF 4.1.4.3.4). Se calcula a partir
 * de {@link DirectCosts} y {@link IndirectCosts} y se persiste para servir
 * reportes sin recalcular en cada lectura.
 */
@Entity
@Table(name = "cost_summaries",
        uniqueConstraints = @UniqueConstraint(name = "uk_cost_summary_batch", columnNames = {"batch_id"}))
public class CostSummary extends AuditableModel {

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "raw_material", nullable = false)
    private Double rawMaterial;

    @Column(name = "direct_labor", nullable = false)
    private Double directLabor;

    @Column(name = "transport", nullable = false)
    private Double transport;

    @Column(name = "storage", nullable = false)
    private Double storage;

    @Column(name = "processing", nullable = false)
    private Double processing;

    @Column(name = "other_costs", nullable = false)
    private Double otherCosts;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "cost_per_kg", nullable = false)
    private Double costPerKg;

    @Column(name = "cost_per_cup", nullable = false)
    private Double costPerCup;

    protected CostSummary() {}

    public CostSummary(Long batchId, DirectCosts direct, IndirectCosts indirect, Double gramsPerCup) {
        this.batchId = batchId;
        recompute(direct, indirect, gramsPerCup);
    }

    public void recompute(DirectCosts direct, IndirectCosts indirect, Double gramsPerCup) {
        if (gramsPerCup == null || gramsPerCup <= 0) {
            throw new IllegalArgumentException("gramsPerCup must be > 0");
        }
        this.rawMaterial = direct.getTotalRawMaterial();
        this.directLabor = direct.getTotalLaborCost();
        this.transport = indirect.getTransport();
        this.storage = indirect.getTotalStorageCost();
        this.processing = round(indirect.getElectricity()
                + indirect.getMachineryMaintenance()
                + indirect.getProcessingSupplies()
                + indirect.getWaterUsed());
        this.otherCosts = round(indirect.getEquipmentDepreciation()
                + indirect.getQualityControl()
                + indirect.getCertifications()
                + indirect.getInsurance()
                + indirect.getAdminExpenses());
        this.total = round(this.rawMaterial + this.directLabor + this.transport
                + this.storage + this.processing + this.otherCosts);
        this.costPerKg = round(this.total / direct.getCoffeeQuantityKg());
        this.costPerCup = round(this.costPerKg * (gramsPerCup / 1000.0));
    }

    private static Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Long getBatchId() { return batchId; }
    public Double getRawMaterial() { return rawMaterial; }
    public Double getDirectLabor() { return directLabor; }
    public Double getTransport() { return transport; }
    public Double getStorage() { return storage; }
    public Double getProcessing() { return processing; }
    public Double getOtherCosts() { return otherCosts; }
    public Double getTotal() { return total; }
    public Double getCostPerKg() { return costPerKg; }
    public Double getCostPerCup() { return costPerCup; }
}
