package com.cafemetrix.cafelab.costing.domain.model.aggregates;

import com.cafemetrix.cafelab.costing.domain.model.commands.CreateBatchCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BatchTest {

    @Test
    void shouldCreateBatchWithProvidedFields() {
        var date = LocalDate.of(2026, 1, 15);
        var batch = new Batch(new CreateBatchCommand(7L, "Lote Catimor", date));
        assertEquals(7L, batch.getUserId());
        assertEquals("Lote Catimor", batch.getBatchName());
        assertEquals(date, batch.getRegistrationDate());
    }

    @Test
    void shouldDefaultRegistrationDateToToday() {
        var batch = new Batch(new CreateBatchCommand(7L, "Lote sin fecha", null));
        assertEquals(LocalDate.now(), batch.getRegistrationDate());
    }

    @Test
    void shouldTrimBatchName() {
        var batch = new Batch(new CreateBatchCommand(7L, "   Lote A  ", LocalDate.now()));
        assertEquals("Lote A", batch.getBatchName());
    }

    @Test
    void shouldRejectBlankBatchName() {
        var command = new CreateBatchCommand(7L, "  ", LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> new Batch(command));
    }

    @Test
    void shouldRenameBatch() {
        var batch = new Batch(new CreateBatchCommand(7L, "Lote A", LocalDate.now()));
        batch.rename("Lote B");
        assertEquals("Lote B", batch.getBatchName());
    }

    @Test
    void shouldRejectBlankRename() {
        var batch = new Batch(new CreateBatchCommand(7L, "Lote A", LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> batch.rename(""));
    }
}
