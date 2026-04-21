package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import java.util.List;
import java.util.Optional;

public interface PlanEntryRepositoryPort {
    Optional<PlanEntry> findById(Integer id);
    List<PlanEntry> findByPlanId(Integer planId);
    PlanEntry save(PlanEntry entry);
    void deleteById(Integer id);
    void deleteByPlanId(Integer planId);
}
