package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByIdQuery;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceCommandService;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceQueryService;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.LotPerformanceResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterLotPerformanceResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.LotPerformanceResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.RegisterLotPerformanceCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/costing", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Costing", description = "Costing Management endpoints")
@SecurityRequirements
public class CostingController {

    private final LotPerformanceCommandService commandService;
    private final LotPerformanceQueryService queryService;

    public CostingController(LotPerformanceCommandService commandService,
                             LotPerformanceQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/lot-performances")
    @Operation(summary = "Register lot performance",
            description = "Records production metrics (yield, loss, time) for a roasted coffee lot")
    public ResponseEntity<LotPerformanceResource> registerLotPerformance(
            @RequestBody RegisterLotPerformanceResource resource) {
        var command = RegisterLotPerformanceCommandFromResourceAssembler.toCommandFromResource(resource);
        var performance = commandService.handle(command);
        return performance
                .map(p -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(LotPerformanceResourceFromEntityAssembler.toResourceFromEntity(p)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/lot-performances")
    @Operation(summary = "Get all lot performances",
            description = "Returns all registered lot performances")
    public ResponseEntity<List<LotPerformanceResource>> getAllLotPerformances() {
        var performances = queryService.handle(new GetAllLotPerformancesQuery());
        var resources = performances.stream()
                .map(LotPerformanceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/lot-performances/coffee-lot/{coffeeLotId}")
    @Operation(summary = "Get lot performances by stored aggregate id",
            description = "Returns all rows whose coffee_lot_id matches the aggregate id (same as id after registration)")
    public ResponseEntity<List<LotPerformanceResource>> getAllLotPerformancesByCoffeeLotId(
            @PathVariable Long coffeeLotId) {
        var performances = queryService.handle(new GetAllLotPerformancesByCoffeeLotIdQuery(coffeeLotId));
        var resources = performances.stream()
                .map(LotPerformanceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/lot-performances/{id}")
    @Operation(summary = "Get lot performance by id",
            description = "Returns the performance record with the given internal id")
    public ResponseEntity<LotPerformanceResource> getLotPerformanceById(@PathVariable Long id) {
        var performance = queryService.handle(new GetLotPerformanceByIdQuery(id));
        return performance
                .map(p -> ResponseEntity.ok(LotPerformanceResourceFromEntityAssembler.toResourceFromEntity(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
