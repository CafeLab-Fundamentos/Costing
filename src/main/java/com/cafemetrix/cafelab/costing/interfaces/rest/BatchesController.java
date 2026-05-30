package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.exceptions.BatchNotFoundException;
import com.cafemetrix.cafelab.costing.domain.model.aggregates.Batch;
import com.cafemetrix.cafelab.costing.domain.model.commands.ComputeBatchCostingCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.DeleteRecommendationCommand;
import com.cafemetrix.cafelab.costing.domain.model.commands.UpdateBatchCommand;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetBatchByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetBatchesByUserIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetCostSummaryByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetDirectCostsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetFinancialIndicatorsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetIndirectCostsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetRecommendationsByBatchIdQuery;
import com.cafemetrix.cafelab.costing.domain.services.BatchCommandService;
import com.cafemetrix.cafelab.costing.domain.services.BatchQueryService;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.AddRecommendationResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.BatchCostingReportResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.ComputeBatchCostingResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.CreateBatchResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterDirectCostsResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterIndirectCostsResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.UpdateBatchResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.BatchResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.CostSummaryResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.CreateBatchCommandFromResourceAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.DirectCostsResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.FinancialIndicatorsResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.IndirectCostsResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.RecommendationResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.RegisterDirectCostsCommandFromResourceAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.RegisterIndirectCostsCommandFromResourceAssembler;
import com.cafemetrix.cafelab.shared.infrastructure.web.ProfileIdResolver;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeproductionContextFacade;
import com.cafemetrix.cafelab.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cafemetrix.cafelab.costing.domain.model.commands.AddRecommendationCommand;

import java.util.Optional;

/**
 * Endpoints del bounded context Costing (PDF 4.1.4.3.4 / 4.1.5.4):
 * Batch y sus entidades hijas (DirectCosts, IndirectCosts, CostSummary,
 * FinancialIndicators, Recommendations). userId se resuelve desde X-User-Id (API Gateway).
 */
@RestController
@RequestMapping(value = "/api/v1/costing/batches", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Costing batches", description = "Batches y cálculo de rentabilidad por lote (US11)")
public class BatchesController {

    private final BatchCommandService commandService;
    private final BatchQueryService queryService;
    private final ProfileIdResolver profileIdResolver;
    private final CoffeeproductionContextFacade coffeeproductionContextFacade;

    public BatchesController(BatchCommandService commandService,
                             BatchQueryService queryService,
                             ProfileIdResolver profileIdResolver,
                             CoffeeproductionContextFacade coffeeproductionContextFacade) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.profileIdResolver = profileIdResolver;
        this.coffeeproductionContextFacade = coffeeproductionContextFacade;
    }

    /** Mismo patrón que CostingController#ownsCoffeeLot: ownership vía ACL hacia Production.
     *  Usa getters estilo bean para mantener compatibilidad cuando este código se porte al
     *  monolito, donde el facade devuelve directamente el agregado {@code CoffeeLot}. */
    private boolean ownsCoffeeLot(Long coffeeLotId, Long currentUserId) {
        return coffeeproductionContextFacade.getCoffeeLotById(coffeeLotId)
                .map(lot -> lot.getUserId() != null && lot.getUserId().equals(currentUserId))
                .orElse(false);
    }

    private Optional<Long> resolveCurrentUserId() {
        return profileIdResolver.resolveProfileId();
    }

    private ResponseEntity<MessageResource> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResource("Falta el header X-User-Id (debe ser inyectado por el API Gateway)"));
    }

    private ResponseEntity<MessageResource> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResource(message));
    }

    private Batch loadOwnedBatch(Long batchId, Long currentUserId) {
        var batch = queryService.handle(new GetBatchByIdQuery(batchId))
                .orElseThrow(() -> new BatchNotFoundException(batchId));
        if (!batch.getUserId().equals(currentUserId)) {
            throw new ForbiddenBatchAccessException();
        }
        return batch;
    }

    // ------------------------------- Batch CRUD -------------------------------

    @Operation(summary = "Crear batch (userId desde X-User-Id)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBatch(@Valid @RequestBody CreateBatchResource resource) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        var command = CreateBatchCommandFromResourceAssembler.toCommandFromResource(current.get(), resource);
        var created = commandService.handle(command)
                .orElseThrow(() -> new IllegalStateException("Could not create batch"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BatchResourceFromEntityAssembler.toResourceFromEntity(created));
    }

    @Operation(summary = "Listar batches del usuario autenticado")
    @GetMapping
    public ResponseEntity<?> listMyBatches() {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        var batches = queryService.handle(new GetBatchesByUserIdQuery(current.get()));
        return ResponseEntity.ok(batches.stream()
                .map(BatchResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @Operation(summary = "Obtener un batch por id (debe ser propio)")
    @GetMapping("/{batchId}")
    public ResponseEntity<?> getBatchById(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        var batch = loadOwnedBatch(batchId, current.get());
        return ResponseEntity.ok(BatchResourceFromEntityAssembler.toResourceFromEntity(batch));
    }

    @Operation(summary = "Renombrar batch (debe ser propio)")
    @PutMapping(value = "/{batchId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateBatch(@PathVariable Long batchId,
                                         @Valid @RequestBody UpdateBatchResource resource) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        var updated = commandService.handle(new UpdateBatchCommand(batchId, resource.batchName()))
                .orElseThrow(() -> new BatchNotFoundException(batchId));
        return ResponseEntity.ok(BatchResourceFromEntityAssembler.toResourceFromEntity(updated));
    }

    @Operation(summary = "Eliminar batch y todas sus entidades hijas")
    @DeleteMapping("/{batchId}")
    public ResponseEntity<?> deleteBatch(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        commandService.deleteBatch(batchId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------ DirectCosts -------------------------------

    @Operation(summary = "Registrar/actualizar costos directos del batch (el lote debe ser del usuario)")
    @PutMapping(value = "/{batchId}/direct-costs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upsertDirectCosts(@PathVariable Long batchId,
                                               @Valid @RequestBody RegisterDirectCostsResource resource) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        if (!ownsCoffeeLot(resource.coffeeLotId(), current.get())) {
            return forbidden("El lote " + resource.coffeeLotId() + " no pertenece a su cuenta");
        }
        var command = RegisterDirectCostsCommandFromResourceAssembler.toCommandFromResource(resource);
        var saved = commandService.handleDirectCosts(batchId, command);
        return ResponseEntity.ok(DirectCostsResourceFromEntityAssembler.toResourceFromEntity(saved));
    }

    @Operation(summary = "Obtener costos directos del batch")
    @GetMapping("/{batchId}/direct-costs")
    public ResponseEntity<?> getDirectCosts(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        return queryService.handle(new GetDirectCostsByBatchIdQuery(batchId))
                .map(d -> ResponseEntity.ok(DirectCostsResourceFromEntityAssembler.toResourceFromEntity(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ------------------------------ IndirectCosts -----------------------------

    @Operation(summary = "Registrar/actualizar costos indirectos del batch")
    @PutMapping(value = "/{batchId}/indirect-costs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upsertIndirectCosts(@PathVariable Long batchId,
                                                 @Valid @RequestBody RegisterIndirectCostsResource resource) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        var command = RegisterIndirectCostsCommandFromResourceAssembler.toCommandFromResource(resource);
        var saved = commandService.handleIndirectCosts(batchId, command);
        return ResponseEntity.ok(IndirectCostsResourceFromEntityAssembler.toResourceFromEntity(saved));
    }

    @Operation(summary = "Obtener costos indirectos del batch")
    @GetMapping("/{batchId}/indirect-costs")
    public ResponseEntity<?> getIndirectCosts(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        return queryService.handle(new GetIndirectCostsByBatchIdQuery(batchId))
                .map(i -> ResponseEntity.ok(IndirectCostsResourceFromEntityAssembler.toResourceFromEntity(i)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --------------------- Cómputo de CostSummary + Indicators ----------------

    @Operation(summary = "Calcular (o recalcular) CostSummary y FinancialIndicators del batch")
    @PostMapping(value = "/{batchId}/compute", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> computeBatchCosting(@PathVariable Long batchId,
                                                 @Valid @RequestBody ComputeBatchCostingResource resource) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        var result = commandService.handle(new ComputeBatchCostingCommand(
                batchId, resource.gramsPerCup(), resource.targetMarginPercentage()));
        return ResponseEntity.ok(new BatchCostingReportResource(
                CostSummaryResourceFromEntityAssembler.toResourceFromEntity(result.costSummary()),
                FinancialIndicatorsResourceFromEntityAssembler.toResourceFromEntity(result.financialIndicators())
        ));
    }

    @Operation(summary = "Obtener CostSummary del batch")
    @GetMapping("/{batchId}/cost-summary")
    public ResponseEntity<?> getCostSummary(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        return queryService.handle(new GetCostSummaryByBatchIdQuery(batchId))
                .map(s -> ResponseEntity.ok(CostSummaryResourceFromEntityAssembler.toResourceFromEntity(s)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener FinancialIndicators del batch")
    @GetMapping("/{batchId}/financial-indicators")
    public ResponseEntity<?> getFinancialIndicators(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        return queryService.handle(new GetFinancialIndicatorsByBatchIdQuery(batchId))
                .map(f -> ResponseEntity.ok(FinancialIndicatorsResourceFromEntityAssembler.toResourceFromEntity(f)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ----------------------------- Recommendations ----------------------------

    @Operation(summary = "Agregar recomendación al batch")
    @PostMapping(value = "/{batchId}/recommendations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addRecommendation(@PathVariable Long batchId,
                                               @Valid @RequestBody AddRecommendationResource resource) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        var saved = commandService.handle(batchId,
                new AddRecommendationCommand(resource.recommendationText()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RecommendationResourceFromEntityAssembler.toResourceFromEntity(saved));
    }

    @Operation(summary = "Listar recomendaciones del batch")
    @GetMapping("/{batchId}/recommendations")
    public ResponseEntity<?> listRecommendations(@PathVariable Long batchId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        var list = queryService.handle(new GetRecommendationsByBatchIdQuery(batchId));
        return ResponseEntity.ok(list.stream()
                .map(RecommendationResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @Operation(summary = "Eliminar una recomendación")
    @DeleteMapping("/{batchId}/recommendations/{recommendationId}")
    public ResponseEntity<?> deleteRecommendation(@PathVariable Long batchId,
                                                  @PathVariable Long recommendationId) {
        var current = resolveCurrentUserId();
        if (current.isEmpty()) return unauthorized();
        loadOwnedBatch(batchId, current.get());
        commandService.handle(new DeleteRecommendationCommand(recommendationId));
        return ResponseEntity.noContent().build();
    }

    /** Excepción interna del controlador para colapsar el chequeo de ownership. */
    static class ForbiddenBatchAccessException extends RuntimeException {
        ForbiddenBatchAccessException() {
            super("Batch no pertenece al usuario autenticado");
        }
    }
}
