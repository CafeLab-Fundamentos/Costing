package com.cafemetrix.cafelab.costing.interfaces.rest.transform;

import com.cafemetrix.cafelab.costing.domain.model.entities.Recommendation;
import com.cafemetrix.cafelab.costing.interfaces.rest.resources.RecommendationResource;

public class RecommendationResourceFromEntityAssembler {

    private RecommendationResourceFromEntityAssembler() {}

    public static RecommendationResource toResourceFromEntity(Recommendation entity) {
        return new RecommendationResource(
                entity.getId(),
                entity.getBatchId(),
                entity.getRecommendationText()
        );
    }
}
