package com.cafemetrix.cafelab.costing.application.internal.commandservices;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.infrastructure.persistence.jpa.repositories.LotPerformanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotPerformanceCommandServiceImplTest {

    @Mock
    private LotPerformanceRepository repository;

    @InjectMocks
    private LotPerformanceCommandServiceImpl service;

    @Test
    void shouldRegisterLotPerformanceSuccessfully() {
        when(repository.existsByCoffeeLotReferenceValue(1L)).thenReturn(false);
        when(repository.save(any(LotPerformance.class))).thenAnswer(i -> i.getArgument(0));

        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60);
        var result = service.handle(command);

        assertTrue(result.isPresent());
        assertEquals(85.0, result.get().getYieldPercentage());
        assertEquals(15.0, result.get().getLossWeight());
        verify(repository).save(any(LotPerformance.class));
    }

    @Test
    void shouldThrowWhenPerformanceAlreadyExistsForLot() {
        when(repository.existsByCoffeeLotReferenceValue(1L)).thenReturn(true);

        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60);

        assertThrows(IllegalArgumentException.class, () -> service.handle(command));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldNotSaveWhenFinalWeightExceedsInitialWeight() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 110.0, 60);
        when(repository.existsByCoffeeLotReferenceValue(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.handle(command));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCalculateCorrectProductivityPerHour() {
        when(repository.existsByCoffeeLotReferenceValue(2L)).thenReturn(false);
        when(repository.save(any(LotPerformance.class))).thenAnswer(i -> i.getArgument(0));

        // 90 kg in 30 min → 180 kg/h
        var command = new RegisterLotPerformanceCommand(2L, 100.0, 90.0, 30);
        var result = service.handle(command);

        assertTrue(result.isPresent());
        assertEquals(180.0, result.get().calculateProductivityPerHour(), 0.01);
    }
}
