package com.cafemetrix.cafelab.costing.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record UpdateBatchResource(
        @NotBlank String batchName
) {}
