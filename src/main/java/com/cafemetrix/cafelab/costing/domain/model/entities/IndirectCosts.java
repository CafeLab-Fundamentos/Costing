package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterIndirectCostsCommand;
import com.cafemetrix.cafelab.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Costos indirectos del Batch (PDF 4.1.4.3.4). Aunque el diagrama indica varios
 * campos como "string", semánticamente son montos monetarios; se modelan como
 * Double para soportar el cálculo de CostSummary y FinancialIndicators.
 */
@Entity
@Table(name = "indirect_costs",
        uniqueConstraints = @UniqueConstraint(name = "uk_indirect_costs_batch", columnNames = {"batch_id"}))
public class IndirectCosts extends AuditableModel {

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "transport", nullable = false)
    private Double transport;

    @Column(name = "storage_days", nullable = false)
    private Integer storageDays;

    @Column(name = "daily_storage_cost", nullable = false)
    private Double dailyStorageCost;

    @Column(name = "total_storage_cost", nullable = false)
    private Double totalStorageCost;

    @Column(name = "electricity", nullable = false)
    private Double electricity;

    @Column(name = "machinery_maintenance", nullable = false)
    private Double machineryMaintenance;

    @Column(name = "processing_supplies", nullable = false)
    private Double processingSupplies;

    @Column(name = "water_used", nullable = false)
    private Double waterUsed;

    @Column(name = "equipment_depreciation", nullable = false)
    private Double equipmentDepreciation;

    @Column(name = "quality_control", nullable = false)
    private Double qualityControl;

    @Column(name = "certifications", nullable = false)
    private Double certifications;

    @Column(name = "insurance", nullable = false)
    private Double insurance;

    @Column(name = "admin_expenses", nullable = false)
    private Double adminExpenses;

    protected IndirectCosts() {}

    public IndirectCosts(Long batchId, RegisterIndirectCostsCommand command) {
        validate(command);
        this.batchId = batchId;
        applyValues(command);
    }

    public void applyUpdate(RegisterIndirectCostsCommand command) {
        validate(command);
        applyValues(command);
    }

    private void applyValues(RegisterIndirectCostsCommand c) {
        this.transport = c.transport();
        this.storageDays = c.storageDays();
        this.dailyStorageCost = c.dailyStorageCost();
        this.electricity = c.electricity();
        this.machineryMaintenance = c.machineryMaintenance();
        this.processingSupplies = c.processingSupplies();
        this.waterUsed = c.waterUsed();
        this.equipmentDepreciation = c.equipmentDepreciation();
        this.qualityControl = c.qualityControl();
        this.certifications = c.certifications();
        this.insurance = c.insurance();
        this.adminExpenses = c.adminExpenses();
        this.totalStorageCost = round(this.dailyStorageCost * this.storageDays);
    }

    private static void validate(RegisterIndirectCostsCommand c) {
        requireNonNegative("transport", c.transport());
        if (c.storageDays() == null || c.storageDays() < 0)
            throw new IllegalArgumentException("storageDays must be >= 0");
        requireNonNegative("dailyStorageCost", c.dailyStorageCost());
        requireNonNegative("electricity", c.electricity());
        requireNonNegative("machineryMaintenance", c.machineryMaintenance());
        requireNonNegative("processingSupplies", c.processingSupplies());
        requireNonNegative("waterUsed", c.waterUsed());
        requireNonNegative("equipmentDepreciation", c.equipmentDepreciation());
        requireNonNegative("qualityControl", c.qualityControl());
        requireNonNegative("certifications", c.certifications());
        requireNonNegative("insurance", c.insurance());
        requireNonNegative("adminExpenses", c.adminExpenses());
    }

    private static void requireNonNegative(String name, Double value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
    }

    private static Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Long getBatchId() { return batchId; }
    public Double getTransport() { return transport; }
    public Integer getStorageDays() { return storageDays; }
    public Double getDailyStorageCost() { return dailyStorageCost; }
    public Double getTotalStorageCost() { return totalStorageCost; }
    public Double getElectricity() { return electricity; }
    public Double getMachineryMaintenance() { return machineryMaintenance; }
    public Double getProcessingSupplies() { return processingSupplies; }
    public Double getWaterUsed() { return waterUsed; }
    public Double getEquipmentDepreciation() { return equipmentDepreciation; }
    public Double getQualityControl() { return qualityControl; }
    public Double getCertifications() { return certifications; }
    public Double getInsurance() { return insurance; }
    public Double getAdminExpenses() { return adminExpenses; }
}
