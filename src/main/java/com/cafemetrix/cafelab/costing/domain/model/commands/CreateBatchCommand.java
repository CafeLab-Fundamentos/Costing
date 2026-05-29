package com.cafemetrix.cafelab.costing.domain.model.commands;

import java.time.LocalDate;

public record CreateBatchCommand(
        Long userId,
        String batchName,
        LocalDate registrationDate
) {}
