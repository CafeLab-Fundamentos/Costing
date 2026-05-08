package com.cafemetrix.cafelab.costing.infrastructure.persistence;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.LotPerformanceRepository;
import com.cafemetrix.cafelab.shared.infrastructure.persistence.jpa.configuration.JpaAuditingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
@Import(JpaAuditingConfiguration.class)
class LotPerformanceRepositoryTest {

    @Autowired
    private LotPerformanceRepository repository;

    @Test
    void shouldPersistAndRetrieveLotPerformanceById() {
        var saved = repository.save(new LotPerformance(
                new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60)));

        assertNotNull(saved.getId());
        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(85.0, found.get().getYieldPercentage());
        assertEquals(15.0, found.get().getLossWeight());
    }

    @Test
    void shouldFindByCoffeeLotReferenceValue() {
        repository.save(new LotPerformance(
                new RegisterLotPerformanceCommand(7L, 50.0, 42.0, 30)));

        var found = repository.findByCoffeeLotReferenceValue(7L);
        assertTrue(found.isPresent());
        assertEquals(7L, found.get().getCoffeeLotId());
    }

    @Test
    void shouldReturnEmptyWhenCoffeeLotNotRegistered() {
        var found = repository.findByCoffeeLotReferenceValue(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenPerformanceExists() {
        repository.save(new LotPerformance(
                new RegisterLotPerformanceCommand(3L, 80.0, 70.0, 45)));

        assertTrue(repository.existsByCoffeeLotReferenceValue(3L));
        assertFalse(repository.existsByCoffeeLotReferenceValue(99L));
    }

    @Test
    void shouldFindMultipleLotsByIdList() {
        repository.save(new LotPerformance(new RegisterLotPerformanceCommand(10L, 100.0, 90.0, 60)));
        repository.save(new LotPerformance(new RegisterLotPerformanceCommand(11L, 100.0, 80.0, 60)));
        repository.save(new LotPerformance(new RegisterLotPerformanceCommand(12L, 100.0, 75.0, 60)));

        var results = repository.findByCoffeeLotReferenceValueIn(List.of(10L, 12L));
        assertEquals(2, results.size());
    }
}
