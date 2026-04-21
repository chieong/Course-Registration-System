package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import java.util.List;

public class PlanReorderRequest {
    private List<Integer> orderedPlanIds;

    public List<Integer> getOrderedPlanIds() {
        return orderedPlanIds;
    }

    public void setOrderedPlanIds(List<Integer> orderedPlanIds) {
        this.orderedPlanIds = orderedPlanIds;
    }
}
