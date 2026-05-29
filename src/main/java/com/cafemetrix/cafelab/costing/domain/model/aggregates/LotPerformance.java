package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.LossWeight;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.ProductionTime;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.YieldPercentage;
import com.cafemetrix.cafelab.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

/**
 * Rendimiento productivo registrado para un lote ya procesado (US14).
 * {@code userId} mantiene la convencion del monolitico: columna {@code user_id} FK logica a
 * {@code profiles.id}. {@code coffeeLotId} referencia {@code coffee_lots.id} del bounded
 * context Production.
 */
@Entity
@Table(name = "lot_performances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"coffee_lot_id"}))
public class LotPerformance extends AuditableAbstractAggregateRoot<LotPerformance> {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "coffee_lot_id", nullable = false))
    private CoffeeLotReference coffeeLotReference;

    @Column(nullable = false)
    private Double initialWeight;

    @Column(nullable = false)
    private Double finalWeight;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "yield_percentage", nullable = false))
    private YieldPercentage yieldPercentage;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "loss_weight", nullable = false))
    private LossWeight lossWeight;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "production_time_minutes", nullable = false))
    private ProductionTime productionTime;

    protected LotPerformance() {}

    public LotPerformance(RegisterLotPerformanceCommand command) {
        if (command.finalWeight() > command.initialWeight()) {
            throw new IllegalArgumentException("Final weight cannot exceed initial weight");
        }
        this.userId = command.userId();
        this.coffeeLotReference = new CoffeeLotReference(command.coffeeLotId());
        this.initialWeight = command.initialWeight();
        this.finalWeight = command.finalWeight();
        this.productionTime = new ProductionTime(command.productionTimeMinutes());
        this.lossWeight = new LossWeight(command.initialWeight() - command.finalWeight());
        this.yieldPercentage = calculateYield();
    }

    private YieldPercentage calculateYield() {
        double yield = (this.finalWeight / this.initialWeight) * 100;
        return new YieldPercentage(Math.round(yield * 100.0) / 100.0);
    }

    public Long getUserId() { return userId; }
    public Double getInitialWeight() { return initialWeight; }
    public Double getFinalWeight() { return finalWeight; }

    /**
     * Same as {@link #getId()} once {@link #assignCoffeeLotIdFromAggregateId()} has run; falls back to {@code id} if unset.
     */
    public Long getCoffeeLotId() {
        return coffeeLotId != null ? coffeeLotId : getId();
    }

    public void assignCoffeeLotIdFromAggregateId() {
        if (getId() != null) {
            this.coffeeLotId = getId();
        }
    }

    public Long getUserId() {
        return userId;
    }

    /**
     * Optional association with an application user (can also be set on registration via command).
     */
    public void assignUserId(Long userId) {
        this.userId = userId;
    }

    public Double getYieldPercentage() {
        return yieldPercentage.value();
    }

    public Double getLossWeight() {
        return lossWeight.value();
    }

    /** % de merma exigido por US14: complemento del yield (100 - yield), redondeado a 2 decimales. */
    public Double getLossPercentage() {
        return Math.round((100.0 - yieldPercentage.value()) * 100.0) / 100.0;
    }

    public Integer getProductionTimeMinutes() {
        return productionTime.minutes();
    }

    /** Productividad horaria (kg/h): {@code finalWeight / minutes * 60}. */
    public Double calculateProductivityPerHour() {
        return Math.round((this.finalWeight / productionTime.minutes()) * 60 * 100.0) / 100.0;
    }
}
