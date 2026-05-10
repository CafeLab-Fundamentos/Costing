package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByIdQuery;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceCommandService;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceQueryService;
import com.cafemetrix.cafelab.shared.infrastructure.persistence.jpa.configuration.JpaAuditingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CostingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfiguration.class))
class CostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotPerformanceCommandService commandService;

    @MockBean
    private LotPerformanceQueryService queryService;

    @Test
    void shouldRegisterLotPerformanceAndReturn201() throws Exception {
        var performance = new LotPerformance(new RegisterLotPerformanceCommand(5L, 100.0, 85.0, 60));
        ReflectionTestUtils.setField(performance, "id", 42L);
        performance.assignCoffeeLotIdFromAggregateId();
        when(commandService.handle(any())).thenReturn(Optional.of(performance));

        var body = """
                {
                  "userId": 5,
                  "initialWeight": 100.0,
                  "finalWeight": 85.0,
                  "productionTimeMinutes": 60
                }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.yieldPercentage").value(85.0))
                .andExpect(jsonPath("$.lossWeight").value(15.0))
                .andExpect(jsonPath("$.coffeeLotId").value(42))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.productivityPerHour").value(85.0));
    }

    @Test
    void shouldReturn404WhenLotPerformanceNotFoundById() throws Exception {
        when(queryService.handle(any(GetLotPerformanceByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/costing/lot-performances/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllLotPerformances() throws Exception {
        var p1 = new LotPerformance(new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60));
        var p2 = new LotPerformance(new RegisterLotPerformanceCommand(1L, 200.0, 170.0, 90));
        when(queryService.handle(any(GetAllLotPerformancesQuery.class))).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/costing/lot-performances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnLotPerformancesByCoffeeLotId() throws Exception {
        var p1 = new LotPerformance(new RegisterLotPerformanceCommand(9L, 100.0, 85.0, 60));
        ReflectionTestUtils.setField(p1, "id", 100L);
        p1.assignCoffeeLotIdFromAggregateId();
        when(queryService.handle(any(GetAllLotPerformancesByCoffeeLotIdQuery.class))).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/v1/costing/lot-performances/coffee-lot/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].coffeeLotId").value(100))
                .andExpect(jsonPath("$[0].userId").value(9));
    }

    @Test
    void shouldReturn400WhenFinalWeightExceedsInitialWeight() throws Exception {
        when(commandService.handle(any())).thenThrow(
                new IllegalArgumentException("Final weight cannot exceed initial weight"));

        var body = """
                {
                  "userId": 1,
                  "initialWeight": 100.0,
                  "finalWeight": 110.0,
                  "productionTimeMinutes": 60
                }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Final weight cannot exceed initial weight"));
    }
}
