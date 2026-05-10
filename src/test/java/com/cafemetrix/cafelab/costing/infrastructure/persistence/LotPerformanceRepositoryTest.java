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

    private LotPerformance saveWithCoffeeLotIdSynced(LotPerformance entity) {
        var saved = repository.save(entity);
        saved.assignCoffeeLotIdFromAggregateId();
        return repository.save(saved);
    }

    @Test
    void shouldPersistAndRetrieveLotPerformanceById() {
        var saved = saveWithCoffeeLotIdSynced(new LotPerformance(
                new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60)));

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), saved.getCoffeeLotId());
        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getCoffeeLotId());
        assertEquals(85.0, found.get().getYieldPercentage());
        assertEquals(15.0, found.get().getLossWeight());
        assertEquals(1L, found.get().getUserId());
    }

    @Test
    void shouldFindByIdUsingPerformanceId() {
        var saved = saveWithCoffeeLotIdSynced(new LotPerformance(
                new RegisterLotPerformanceCommand(1L, 50.0, 42.0, 30)));

        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getCoffeeLotId());
    }

    @Test
    void shouldReturnEmptyWhenIdNotRegistered() {
        var found = repository.findById(999_999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenPerformanceExists() {
        var saved = saveWithCoffeeLotIdSynced(new LotPerformance(
                new RegisterLotPerformanceCommand(1L, 80.0, 70.0, 45)));

        assertTrue(repository.existsById(saved.getId()));
        assertFalse(repository.existsById(99L));
    }

    @Test
    void shouldFindMultipleByIdList() {
        var a = saveWithCoffeeLotIdSynced(new LotPerformance(new RegisterLotPerformanceCommand(1L, 100.0, 90.0, 60)));
        var b = saveWithCoffeeLotIdSynced(new LotPerformance(new RegisterLotPerformanceCommand(1L, 100.0, 80.0, 60)));
        var c = saveWithCoffeeLotIdSynced(new LotPerformance(new RegisterLotPerformanceCommand(1L, 100.0, 75.0, 60)));

        var results = repository.findAllById(List.of(a.getId(), c.getId()));
        assertEquals(2, results.size());
    }

    @Test
    void shouldFindByCoffeeLotId() {
        var saved = saveWithCoffeeLotIdSynced(new LotPerformance(
                new RegisterLotPerformanceCommand(1L, 100.0, 90.0, 60)));

        var list = repository.findByCoffeeLotId(saved.getId());
        assertEquals(1, list.size());
        assertEquals(saved.getId(), list.get(0).getId());
    }

    @Test
    void shouldFindAllByUserId() {
        var a = new LotPerformance(new RegisterLotPerformanceCommand(50L, 100.0, 90.0, 60));
        repository.save(a);
        var b = new LotPerformance(new RegisterLotPerformanceCommand(50L, 100.0, 80.0, 60));
        repository.save(b);
        var c = new LotPerformance(new RegisterLotPerformanceCommand(51L, 100.0, 75.0, 60));
        repository.save(c);

        var forUser50 = repository.findByUserId(50L);
        assertEquals(2, forUser50.size());
        assertTrue(forUser50.stream().allMatch(p -> Long.valueOf(50L).equals(p.getUserId())));
    }
}
