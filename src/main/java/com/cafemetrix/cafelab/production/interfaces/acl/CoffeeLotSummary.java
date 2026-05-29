package com.cafemetrix.cafelab.production.interfaces.acl;

/**
 * Resumen del lote consumido desde el monolitico. Solo lo que Costing necesita
 * (id y dueño) para validar ownership; no replica el agregado completo.
 *
 * <p>Modelado como clase con getters estilo bean (en vez de {@code record})
 * para mantener parity con el agregado {@code CoffeeLot} del monolito. Así, al
 * portar Costing al monolito, los call sites pueden cambiar la implementación
 * del facade (que ahora devolverá un {@code CoffeeLot} con {@code getUserId()})
 * sin tocar el código de Costing.</p>
 */
public class CoffeeLotSummary {

    private final Long id;
    private final Long userId;

    public CoffeeLotSummary(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }
}
