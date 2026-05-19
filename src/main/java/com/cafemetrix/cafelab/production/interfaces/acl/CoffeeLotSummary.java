package com.cafemetrix.cafelab.production.interfaces.acl;

/**
 * Resumen del lote consumido desde el monolitico. Solo lo que Costing necesita
 * (id y dueno) para validar ownership; no replica el agregado completo.
 */
public record CoffeeLotSummary(Long id, Long userId) {}
