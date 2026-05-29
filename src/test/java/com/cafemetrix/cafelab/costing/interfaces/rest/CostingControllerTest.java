package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.LotPerformance;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterLotPerformanceCommand;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformancesByUserIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetPerformanceComparisonQuery;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceCommandService;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceQueryService;
import com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.support.CurrentProfileIdResolver;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeLotSummary;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeproductionContextFacade;
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

    private static final Long OWNER = 99L;
    private static final Long OTHER_OWNER = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotPerformanceCommandService commandService;

    @MockBean
    private LotPerformanceQueryService queryService;

    @MockBean
    private CurrentProfileIdResolver currentProfileIdResolver;

    @MockBean
    private CoffeeproductionContextFacade coffeeproductionContextFacade;

    private LotPerformance ownerPerformance(Long coffeeLotId, Double initial, Double finalW, Integer mins) {
        return new LotPerformance(new RegisterLotPerformanceCommand(OWNER, coffeeLotId, initial, finalW, mins));
    }

    @Test
    void shouldRegisterLotPerformanceAndReturn201() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OWNER)));
        when(commandService.handle(any())).thenReturn(Optional.of(ownerPerformance(1L, 100.0, 85.0, 60)));

        var body = """
                { "coffeeLotId": 1, "initialWeight": 100.0, "finalWeight": 85.0, "productionTimeMinutes": 60 }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(OWNER))
                .andExpect(jsonPath("$.coffeeLotId").value(1))
                .andExpect(jsonPath("$.yieldPercentage").value(85.0))
                .andExpect(jsonPath("$.lossWeight").value(15.0))
                .andExpect(jsonPath("$.lossPercentage").value(15.0))
                .andExpect(jsonPath("$.productivityPerHour").value(85.0));
    }

    @Test
    void shouldReturn401WhenProfileCannotBeResolved() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/costing/lot-performances"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenRegisteringPerformanceForForeignLot() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OTHER_OWNER)));

        var body = """
                { "coffeeLotId": 1, "initialWeight": 100.0, "finalWeight": 85.0, "productionTimeMinutes": 60 }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn404WhenLotPerformanceNotFoundById() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetLotPerformanceByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/costing/lot-performances/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenFetchingForeignLotPerformanceById() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        var foreign = new LotPerformance(
                new RegisterLotPerformanceCommand(OTHER_OWNER, 1L, 100.0, 85.0, 60));
        when(queryService.handle(any(GetLotPerformanceByIdQuery.class))).thenReturn(Optional.of(foreign));

        mockMvc.perform(get("/api/v1/costing/lot-performances/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListOnlyOwnLotPerformances() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        var p1 = ownerPerformance(1L, 100.0, 85.0, 60);
        var p2 = ownerPerformance(2L, 200.0, 170.0, 90);
        when(queryService.handle(any(GetLotPerformancesByUserIdQuery.class))).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/costing/lot-performances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(OWNER));
    }

    @Test
    void shouldFetchLotPerformanceByCoffeeLotIdWhenOwned() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OWNER)));
        when(queryService.handle(any(GetLotPerformanceByCoffeeLotIdQuery.class)))
                .thenReturn(Optional.of(ownerPerformance(1L, 100.0, 85.0, 60)));

        mockMvc.perform(get("/api/v1/costing/lot-performances/coffee-lot/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coffeeLotId").value(1));
    }

    @Test
    void shouldReturn400WhenComparingLessThanTwoLots() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));

        mockMvc.perform(get("/api/v1/costing/lot-performances/comparison").param("ids", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnComparisonForOwnedLots() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OWNER)));
        when(coffeeproductionContextFacade.getCoffeeLotById(2L))
                .thenReturn(Optional.of(new CoffeeLotSummary(2L, OWNER)));
        when(queryService.handle(any(GetPerformanceComparisonQuery.class)))
                .thenReturn(List.of(
                        ownerPerformance(1L, 100.0, 85.0, 60),
                        ownerPerformance(2L, 100.0, 90.0, 60)));

        mockMvc.perform(get("/api/v1/costing/lot-performances/comparison").param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRejectComparisonWhenAnyLotIsForeign() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OWNER)));
        when(coffeeproductionContextFacade.getCoffeeLotById(2L))
                .thenReturn(Optional.of(new CoffeeLotSummary(2L, OTHER_OWNER)));

        mockMvc.perform(get("/api/v1/costing/lot-performances/comparison").param("ids", "1", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400WhenFinalWeightExceedsInitialWeight() throws Exception {
        when(currentProfileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OWNER)));
        when(commandService.handle(any())).thenThrow(
                new IllegalArgumentException("Final weight cannot exceed initial weight"));

        var body = """
                { "coffeeLotId": 1, "initialWeight": 100.0, "finalWeight": 110.0, "productionTimeMinutes": 60 }
                """;

        mockMvc.perform(post("/api/v1/costing/lot-performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Final weight cannot exceed initial weight"));
    }
}
