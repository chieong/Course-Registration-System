package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationPlanRepository extends JpaRepository<RegistrationPlan, Integer> {
    @Query("select p from RegistrationPlan p where p.student.studentId = :studentId")
    List<RegistrationPlan> findByStudentId(@Param("studentId") Integer studentId);

    @Query("select p from RegistrationPlan p where p.student.studentId = :studentId order by p.priority asc")
    List<RegistrationPlan> findByStudentIdOrderByPriorityAsc(@Param("studentId") Integer studentId);

    @Query("select count(p) from RegistrationPlan p where p.student.studentId = :studentId")
    long countByStudentId(@Param("studentId") Integer studentId);

    @Query("select count(p) from RegistrationPlan p where p.student.studentId = :studentId")
    long countByStudentIdForPlanLimit(@Param("studentId") Integer studentId);

    @Query("select case when count(p) > 0 then true else false end from RegistrationPlan p where p.student.studentId = :studentId and p.priority = :priority")
    boolean existsByStudentIdAndPriority(@Param("studentId") Integer studentId,
                                         @Param("priority") Integer priority);

    @Query("select p from RegistrationPlan p where p.student.studentId = :studentId  and p.priority = :priority")
    Optional<RegistrationPlan> findByStudentIdAndPriority(@Param("studentId") Integer studentId,
                                                                 @Param("priority") Integer priority);

    @Query("select p from RegistrationPlan p where  p.student.cohort = :cohort order by p.student.studentId asc, p.priority asc")
    List<RegistrationPlan> findByCohortOrderByStudentAndPriority(@Param("cohort") Integer cohort);

}
