package com.cafemetrix.cafelab.production.interfaces.acl;

import java.util.Optional;

/**
 * Anti-corruption layer hacia el bounded context Production (vive en el monolitico).
 * Costing solo consulta el lote para validar existencia y ownership; nunca lo modifica.
 */
public interface CoffeeproductionContextFacade {

    Optional<CoffeeLotSummary> getCoffeeLotById(Long coffeeLotId);
}
