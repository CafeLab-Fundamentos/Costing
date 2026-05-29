package com.cafemetrix.cafelab.costing.interfaces.rest;

import com.cafemetrix.cafelab.costing.domain.exceptions.LotPerformanceNotFoundException;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetAllLotPerformancesQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByCoffeeLotIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformanceByIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetLotPerformancesByUserIdQuery;
import com.cafemetrix.cafelab.costing.domain.model.queries.GetPerformanceComparisonQuery;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceCommandService;
import com.cafemetrix.cafelab.costing.domain.services.LotPerformanceQueryService;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.LotPerformanceResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RegisterLotPerformanceResource;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.LotPerformanceResourceFromEntityAssembler;
import com.cafemetrix.cafelab.costing.interfaces.rest.transform.RegisterLotPerformanceCommandFromResourceAssembler;
import com.cafemetrix.cafelab.iam.infrastructure.authorization.sfs.support.CurrentProfileIdResolver;
import com.cafemetrix.cafelab.production.interfaces.acl.CoffeeproductionContextFacade;
import com.cafemetrix.cafelab.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoints de US14 (Analisis de Eficiencia y Rendimiento). Patron de autorizacion
 * alineado con el monolitico (CoffeeLotsController, InventoryEntriesController):
 * el {@code userId} viene del JWT y la pertenencia del lote se valida via ACL hacia Production.
 */
@RestController
@RequestMapping(value = "/api/v1/costing/lot-performances", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Costing", description = "Lot performance metrics (US14)")
public class CostingController {

    private final LotPerformanceCommandService commandService;
    private final LotPerformanceQueryService queryService;
    private final CurrentProfileIdResolver currentProfileIdResolver;
    private final CoffeeproductionContextFacade coffeeproductionContextFacade;

    public CostingController(LotPerformanceCommandService commandService,
                             LotPerformanceQueryService queryService,
                             CurrentProfileIdResolver currentProfileIdResolver,
                             CoffeeproductionContextFacade coffeeproductionContextFacade) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.currentProfileIdResolver = currentProfileIdResolver;
        this.coffeeproductionContextFacade = coffeeproductionContextFacade;
    }

    private Optional<Long> resolveCurrentUserId() {
        return currentProfileIdResolver.resolveProfileId();
    }

    private ResponseEntity<MessageResource> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResource(message));
    }

    private ResponseEntity<MessageResource> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResource(message));
    }

    /** Mismo patron que el monolitico (CoffeeLotsController#ownsCoffeeLot). Usa getters
     *  estilo bean para mantener compatibilidad al portar este código al monolito, donde el
     *  facade devuelve directamente el agregado {@code CoffeeLot}. */
    private boolean ownsCoffeeLot(Long coffeeLotId, Long currentUserId) {
        return coffeeproductionContextFacade.getCoffeeLotById(coffeeLotId)
                .map(lot -> lot.getUserId() != null && lot.getUserId().equals(currentUserId))
                .orElse(false);
    }

    @Operation(summary = "Registrar rendimiento de un lote (userId desde JWT; lote debe ser suyo)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerLotPerformance(@Valid @RequestBody RegisterLotPerformanceResource resource) {
        Optional<Long> ownerIdOpt = resolveCurrentUserId();
        if (ownerIdOpt.isEmpty()) {
            return unauthorized("Usuario no autenticado o perfil no encontrado");
        }
        Long ownerId = ownerIdOpt.get();
        if (!ownsCoffeeLot(resource.coffeeLotId(), ownerId)) {
            return forbidden("Debe registrar rendimiento de un lote de su cuenta");
        }
        var command = RegisterLotPerformanceCommandFromResourceAssembler.toCommandFromResource(ownerId, resource);
        var performance = commandService.handle(command);
        return performance
                .map(p -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(LotPerformanceResourceFromEntityAssembler.toResourceFromEntity(p)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Listar rendimientos del usuario autenticado")
    @GetMapping
    public ResponseEntity<?> getAllLotPerformances() {
        Optional<Long> userIdOpt = resolveCurrentUserId();
        if (userIdOpt.isEmpty()) {
            return unauthorized("Usuario no autenticado o perfil no encontrado");
        }
        var performances = queryService.handle(new GetLotPerformancesByUserIdQuery(userIdOpt.get()));
        return ResponseEntity.ok(performances.stream()
                .map(LotPerformanceResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @Operation(summary = "Obtener rendimiento por id (debe ser suyo)")
    @GetMapping("/{id}")
    public ResponseEntity<?> getLotPerformanceById(@PathVariable Long id) {
        Optional<Long> currentOpt = resolveCurrentUserId();
        if (currentOpt.isEmpty()) {
            return unauthorized("Usuario no autenticado o perfil no encontrado");
        }
        var performance = queryService.handle(new GetLotPerformanceByIdQuery(id));
        if (performance.isEmpty()) {
            throw new LotPerformanceNotFoundException(id);
        }
        if (!performance.get().getUserId().equals(currentOpt.get())) {
            return forbidden("No autorizado para ver este rendimiento");
        }
        return ResponseEntity.ok(LotPerformanceResourceFromEntityAssembler.toResourceFromEntity(performance.get()));
    }

    @Operation(summary = "Obtener rendimiento por coffeeLotId (lote debe ser suyo)")
    @GetMapping("/coffee-lot/{coffeeLotId}")
    public ResponseEntity<?> getLotPerformanceByCoffeeLotId(@PathVariable Long coffeeLotId) {
        Optional<Long> currentOpt = resolveCurrentUserId();
        if (currentOpt.isEmpty()) {
            return unauthorized("Usuario no autenticado o perfil no encontrado");
        }
        if (!ownsCoffeeLot(coffeeLotId, currentOpt.get())) {
            return forbidden("No autorizado para consultar este lote");
        }
        var performance = queryService.handle(new GetLotPerformanceByCoffeeLotIdQuery(coffeeLotId));
        return performance
                .map(p -> ResponseEntity.ok(LotPerformanceResourceFromEntityAssembler.toResourceFromEntity(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Rendimientos por userId (solo el propio perfil)")
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getLotPerformancesByUserId(@PathVariable Long userId) {
        Optional<Long> currentOpt = resolveCurrentUserId();
        if (currentOpt.isEmpty()) {
            return unauthorized("Usuario no autenticado o perfil no encontrado");
        }
        if (!currentOpt.get().equals(userId)) {
            return forbidden("No puede consultar rendimientos de otro perfil");
        }
        var performances = queryService.handle(new GetLotPerformancesByUserIdQuery(userId));
        return ResponseEntity.ok(performances.stream()
                .map(LotPerformanceResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @Operation(summary = "Comparativa de rendimiento entre varios lotes propios (US14 escenario 2)")
    @GetMapping("/comparison")
    public ResponseEntity<?> compareLotPerformances(@RequestParam("ids") List<Long> coffeeLotIds) {
        Optional<Long> currentOpt = resolveCurrentUserId();
        if (currentOpt.isEmpty()) {
            return unauthorized("Usuario no autenticado o perfil no encontrado");
        }
        if (coffeeLotIds == null || coffeeLotIds.size() < 2) {
            return ResponseEntity.badRequest()
                    .body(new MessageResource("Se requieren al menos dos coffeeLotIds para comparar"));
        }
        Long ownerId = currentOpt.get();
        for (Long lotId : coffeeLotIds) {
            if (!ownsCoffeeLot(lotId, ownerId)) {
                return forbidden("El lote " + lotId + " no pertenece a su cuenta");
            }
        }
        var performances = queryService.handle(new GetPerformanceComparisonQuery(coffeeLotIds));
        return ResponseEntity.ok(performances.stream()
                .map(LotPerformanceResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
