package com.cafemetrix.cafelab.costing.domain.model.entities;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import com.cafemetrix.cafelab.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Costos directos del Batch (PDF 4.1.4.3.4): materia prima y mano de obra.
 * Mantiene los totales como columnas derivadas calculadas en cada actualización
 * para evitar recálculos en la capa de aplicación.
 */
@Entity
@Table(name = "direct_costs",
        uniqueConstraints = @UniqueConstraint(name = "uk_direct_costs_batch", columnNames = {"batch_id"}))
public class DirectCosts extends AuditableModel {

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "coffee_lot_id", nullable = false)
    private Long coffeeLotId;

    @Column(name = "raw_material_cost", nullable = false)
    private Double rawMaterialCost;

    @Column(name = "coffee_quantity_kg", nullable = false)
    private Double coffeeQuantityKg;

    @Column(name = "total_raw_material", nullable = false)
    private Double totalRawMaterial;

    @Column(name = "hours_worked", nullable = false)
    private Integer hoursWorked;

    @Column(name = "cost_per_hour", nullable = false)
    private Double costPerHour;

    @Column(name = "num_workers", nullable = false)
    private Integer numWorkers;

    @Column(name = "total_labor_cost", nullable = false)
    private Double totalLaborCost;

    protected DirectCosts() {}

    public DirectCosts(Long batchId, RegisterDirectCostsCommand command) {
        validate(command);
        this.batchId = batchId;
        applyValues(command);
    }

    public void applyUpdate(RegisterDirectCostsCommand command) {
        validate(command);
        applyValues(command);
    }

    private void applyValues(RegisterDirectCostsCommand command) {
        this.coffeeLotId = command.coffeeLotId();
        this.rawMaterialCost = command.rawMaterialCost();
        this.coffeeQuantityKg = command.coffeeQuantityKg();
        this.hoursWorked = command.hoursWorked();
        this.costPerHour = command.costPerHour();
        this.numWorkers = command.numWorkers();
        this.totalRawMaterial = round(this.rawMaterialCost * this.coffeeQuantityKg);
        this.totalLaborCost = round(this.hoursWorked * this.costPerHour * this.numWorkers);
    }

    private static void validate(RegisterDirectCostsCommand c) {
        if (c.coffeeLotId() == null)
            throw new IllegalArgumentException("coffeeLotId is required");
        if (c.rawMaterialCost() == null || c.rawMaterialCost() < 0)
            throw new IllegalArgumentException("rawMaterialCost must be >= 0");
        if (c.coffeeQuantityKg() == null || c.coffeeQuantityKg() <= 0)
            throw new IllegalArgumentException("coffeeQuantityKg must be > 0");
        if (c.hoursWorked() == null || c.hoursWorked() < 0)
            throw new IllegalArgumentException("hoursWorked must be >= 0");
        if (c.costPerHour() == null || c.costPerHour() < 0)
            throw new IllegalArgumentException("costPerHour must be >= 0");
        if (c.numWorkers() == null || c.numWorkers() < 0)
            throw new IllegalArgumentException("numWorkers must be >= 0");
    }

    private static Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Long getBatchId() { return batchId; }
    public Long getCoffeeLotId() { return coffeeLotId; }
    public Double getRawMaterialCost() { return rawMaterialCost; }
    public Double getCoffeeQuantityKg() { return coffeeQuantityKg; }
    public Double getTotalRawMaterial() { return totalRawMaterial; }
    public Integer getHoursWorked() { return hoursWorked; }
    public Double getCostPerHour() { return costPerHour; }
    public Integer getNumWorkers() { return numWorkers; }
    public Double getTotalLaborCost() { return totalLaborCost; }
}
