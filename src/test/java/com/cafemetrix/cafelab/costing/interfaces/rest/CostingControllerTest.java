package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
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
        var performance = new LotPerformance(new RegisterLotPerformanceCommand(1L, 100.0, 85.0, 60));
        when(commandService.handle(any())).thenReturn(Optional.of(performance));

        var body = """
                {
                  "coffeeLotId": 1,
                  "initialWeight": 100.0,
                  "finalWeight": 85.0,
                  "productionTimeMinutes": 60
                }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.yieldPercentage").value(85.0))
                .andExpect(jsonPath("$.lossWeight").value(15.0))
                .andExpect(jsonPath("$.coffeeLotId").value(1))
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
        var p2 = new LotPerformance(new RegisterLotPerformanceCommand(2L, 200.0, 170.0, 90));
        when(queryService.handle(any(GetAllLotPerformancesQuery.class))).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/costing/lot-performances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn400WhenFinalWeightExceedsInitialWeight() throws Exception {
        when(commandService.handle(any())).thenThrow(
                new IllegalArgumentException("Final weight cannot exceed initial weight"));

        var body = """
                {
                  "coffeeLotId": 1,
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

    @Test
    void shouldReturn400WhenLotPerformanceAlreadyRegistered() throws Exception {
        when(commandService.handle(any())).thenThrow(
                new IllegalArgumentException("Performance for coffee lot with id 1 is already registered"));

        var body = """
                {
                  "coffeeLotId": 1,
                  "initialWeight": 100.0,
                  "finalWeight": 85.0,
                  "productionTimeMinutes": 60
                }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
