package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.LossWeight;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.ProductionTime;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.YieldPercentage;
import com.cafemetrix.cafelab.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

@Entity
public class LotPerformance extends AuditableAbstractAggregateRoot<LotPerformance> {

    /**
     * Legacy column name: stores the same value as {@link #getId()} after persist (aggregate id).
     */
    @Column(name = "coffee_lot_id")
    private Long coffeeLotId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private Double initialWeight;

    @Column(nullable = false)
    private Double finalWeight;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "yield_percentage"))
    private YieldPercentage yieldPercentage;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "loss_weight"))
    private LossWeight lossWeight;

    @Embedded
    @AttributeOverride(name = "minutes", column = @Column(name = "production_time_minutes"))
    private ProductionTime productionTime;

    public LotPerformance() {}

    public LotPerformance(RegisterLotPerformanceCommand command) {
        if (command.finalWeight() > command.initialWeight()) {
            throw new IllegalArgumentException("Final weight cannot exceed initial weight");
        }
        this.userId = command.userId();
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

    public Integer getProductionTimeMinutes() {
        return productionTime.minutes();
    }

    public Double calculateProductivityPerHour() {
        if (productionTime.minutes() == 0) return 0.0;
        return (this.finalWeight / productionTime.minutes()) * 60;
    }
}
