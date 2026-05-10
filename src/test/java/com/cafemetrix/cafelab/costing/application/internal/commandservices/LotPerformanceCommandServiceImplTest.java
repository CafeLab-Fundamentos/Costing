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
        when(repository.save(any(LotPerformance.class))).thenAnswer(i -> i.getArgument(0));

        var command = new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60);
        var result = service.handle(command);

        assertTrue(result.isPresent());
        assertEquals(85.0, result.get().getYieldPercentage());
        assertEquals(15.0, result.get().getLossWeight());
        assertEquals(1L, result.get().getUserId());
        verify(repository, times(2)).save(any(LotPerformance.class));
    }

    @Test
    void shouldNotSaveWhenFinalWeightExceedsInitialWeight() {
        var command = new RegisterLotPerformanceCommand(1L, 100.0, 110.0, 60);

        assertThrows(IllegalArgumentException.class, () -> service.handle(command));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCalculateCorrectProductivityPerHour() {
        when(repository.save(any(LotPerformance.class))).thenAnswer(i -> i.getArgument(0));

        var command = new RegisterLotPerformanceCommand(1L, 100.0, 90.0, 30);
        var result = service.handle(command);

        assertTrue(result.isPresent());
        assertEquals(180.0, result.get().calculateProductivityPerHour(), 0.01);
    }
}
