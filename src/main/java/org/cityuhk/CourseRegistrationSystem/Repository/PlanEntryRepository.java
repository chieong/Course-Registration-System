package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.PlanEntryRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlanEntryRepository extends JpaRepository<PlanEntry, Integer>, PlanEntryRepositoryPort {

    @Query("select e from PlanEntry e where e.plan.planId = :planId")
    List<PlanEntry> findByPlanId(@Param("planId") Integer planId);

    @Transactional
    @Modifying
    @Query("delete from PlanEntry e where e.plan.planId = :planId")
    void deleteByPlanId(@Param("planId") Integer planId);
}
