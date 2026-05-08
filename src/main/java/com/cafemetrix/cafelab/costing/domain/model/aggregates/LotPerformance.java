package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.model.valueobjects.*;
import com.cafemetrix.cafelab.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

@Entity
public class LotPerformance extends AuditableAbstractAggregateRoot<LotPerformance> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "coffee_lot_id"))
    private CoffeeLotReference coffeeLotReference;

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

    public Double getInitialWeight() { return initialWeight; }
    public Double getFinalWeight() { return finalWeight; }

    public Long getCoffeeLotId() {
        return coffeeLotReference.value();
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
