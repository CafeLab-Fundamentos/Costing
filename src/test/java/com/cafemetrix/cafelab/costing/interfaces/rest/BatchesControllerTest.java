package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.model.aggregates.Batch;
import com.cafemetrix.cafelab.costing.domain.model.commands.CreateBatchCommand;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetBatchByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetBatchesByUserIdQuery;
import com.cafemetrix.cafelab.costing.domain.services.BatchCommandService;
import com.cafemetrix.cafelab.costing.domain.services.BatchQueryService;
import com.cafemetrix.cafelab.costing.domain.model.commands.RegisterDirectCostsCommand;
import com.cafemetrix.cafelab.costing.domain.model.entities.DirectCosts;
import com.cafemetrix.cafelab.shared.infrastructure.web.ProfileIdResolver;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeLotSummary;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeproductionContextFacade;
import com.cafemetrix.cafelab.shared.infrastructure.persistence.jpa.configuration.JpaAuditingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BatchesController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JpaAuditingConfiguration.class))
@AutoConfigureMockMvc(addFilters = false)
class BatchesControllerTest {

    private static final Long OWNER = 7L;
    private static final Long OTHER = 99L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchCommandService commandService;

    @MockBean
    private BatchQueryService queryService;

    @MockBean
    private ProfileIdResolver profileIdResolver;

    @MockBean
    private CoffeeproductionContextFacade coffeeproductionContextFacade;

    private Batch ownerBatch() {
        return new Batch(new CreateBatchCommand(OWNER, "Lote A", LocalDate.of(2026, 1, 15)));
    }

    private Batch foreignBatch() {
        return new Batch(new CreateBatchCommand(OTHER, "Lote ajeno", LocalDate.of(2026, 1, 15)));
    }

    @Test
    void shouldReturn401WhenProfileCannotBeResolved() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/costing/batches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateBatchWithUserIdFromJwt() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(commandService.handle(any(CreateBatchCommand.class)))
                .thenReturn(Optional.of(ownerBatch()));

        var body = """
                { "batchName": "Lote A", "registrationDate": "2026-01-15" }
                """;

        mockMvc.perform(post("/api/v1/costing/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(OWNER))
                .andExpect(jsonPath("$.batchName").value("Lote A"));
    }

    @Test
    void shouldReturn400WhenBatchNameIsBlank() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));

        var body = """
                { "batchName": "", "registrationDate": "2026-01-15" }
                """;

        mockMvc.perform(post("/api/v1/costing/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListOnlyOwnBatches() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchesByUserIdQuery.class)))
                .thenReturn(List.of(ownerBatch()));

        mockMvc.perform(get("/api/v1/costing/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(OWNER));
    }

    @Test
    void shouldReturn404WhenBatchNotFound() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/costing/batches/55"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenFetchingForeignBatch() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchByIdQuery.class)))
                .thenReturn(Optional.of(foreignBatch()));

        mockMvc.perform(get("/api/v1/costing/batches/55"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpsertDirectCostsWhenLotBelongsToUser() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchByIdQuery.class)))
                .thenReturn(Optional.of(ownerBatch()));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OWNER)));
        when(commandService.handleDirectCosts(any(), any()))
                .thenReturn(new DirectCosts(10L,
                        new RegisterDirectCostsCommand(1L, 20.0, 50.0, 8, 12.5, 3)));

        var body = """
                { "coffeeLotId": 1, "rawMaterialCost": 20.0, "coffeeQuantityKg": 50.0,
                  "hoursWorked": 8, "costPerHour": 12.5, "numWorkers": 3 }
                """;

        mockMvc.perform(put("/api/v1/costing/batches/10/direct-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coffeeLotId").value(1))
                .andExpect(jsonPath("$.totalRawMaterial").value(1000.0))
                .andExpect(jsonPath("$.totalLaborCost").value(300.0));
    }

    @Test
    void shouldReturn403WhenDirectCostsCoffeeLotIsForeign() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchByIdQuery.class)))
                .thenReturn(Optional.of(ownerBatch()));
        when(coffeeproductionContextFacade.getCoffeeLotById(1L))
                .thenReturn(Optional.of(new CoffeeLotSummary(1L, OTHER)));

        var body = """
                { "coffeeLotId": 1, "rawMaterialCost": 20.0, "coffeeQuantityKg": 50.0,
                  "hoursWorked": 8, "costPerHour": 12.5, "numWorkers": 3 }
                """;

        mockMvc.perform(put("/api/v1/costing/batches/10/direct-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400WhenDirectCostsMissingCoffeeLotId() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchByIdQuery.class)))
                .thenReturn(Optional.of(ownerBatch()));

        var body = """
                { "rawMaterialCost": 20.0, "coffeeQuantityKg": 50.0,
                  "hoursWorked": 8, "costPerHour": 12.5, "numWorkers": 3 }
                """;

        mockMvc.perform(put("/api/v1/costing/batches/10/direct-costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenComputingWithoutCosts() throws Exception {
        when(profileIdResolver.resolveProfileId()).thenReturn(Optional.of(OWNER));
        when(queryService.handle(any(GetBatchByIdQuery.class)))
                .thenReturn(Optional.of(ownerBatch()));
        when(commandService.handle(any(com.cafemetrix.cafelab.costing.domain.model.commands.ComputeBatchCostingCommand.class)))
                .thenThrow(new IllegalStateException(
                        "DirectCosts must be registered before computing the batch costing"));

        var body = """
                { "gramsPerCup": 18.0, "targetMarginPercentage": 25.0 }
                """;

        mockMvc.perform(post("/api/v1/costing/batches/1/compute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("DirectCosts")));
    }
}
