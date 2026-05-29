package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateBatchResource(
        @NotBlank String batchName,
        LocalDate registrationDate
) {}
