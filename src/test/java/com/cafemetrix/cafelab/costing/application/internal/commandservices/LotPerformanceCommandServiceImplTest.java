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

    private static RegisterLotPerformanceCommand cmd(Long coffeeLotId, Double initial, Double finalW, Integer minutes) {
        return new RegisterLotPerformanceCommand(99L, coffeeLotId, initial, finalW, minutes);
    }

    @Test
    void shouldRegisterLotPerformanceSuccessfully() {
        when(repository.save(any(LotPerformance.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.handle(cmd(1L, 100.0, 85.0, 60));

        assertTrue(result.isPresent());
        assertEquals(85.0, result.get().getYieldPercentage());
        assertEquals(15.0, result.get().getLossWeight());
        assertEquals(99L, result.get().getUserId());
        verify(repository).save(any(LotPerformance.class));
    }

    @Test
    void shouldThrowWhenPerformanceAlreadyExistsForLot() {
        when(repository.existsByCoffeeLotReferenceValue(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.handle(cmd(1L, 100.0, 85.0, 60)));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldNotSaveWhenFinalWeightExceedsInitialWeight() {
        when(repository.existsByCoffeeLotReferenceValue(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.handle(cmd(1L, 100.0, 110.0, 60)));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCalculateCorrectProductivityPerHour() {
        when(repository.save(any(LotPerformance.class))).thenAnswer(i -> i.getArgument(0));

        // 90 kg / 30 min * 60 = 180 kg/h
        var result = service.handle(cmd(2L, 100.0, 90.0, 30));

        assertTrue(result.isPresent());
        assertEquals(180.0, result.get().calculateProductivityPerHour(), 0.01);
    }
}
