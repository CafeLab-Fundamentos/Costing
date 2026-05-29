package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import java.time.LocalDate;

public record BatchResource(
        Long id,
        Long userId,
        String batchName,
        LocalDate registrationDate
) {}
