package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationPlanRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrationPlanRepository extends JpaRepository<RegistrationPlan, Integer>, RegistrationPlanRepositoryPort {

    @Query("select p from RegistrationPlan p where p.student.studentId = :studentId")
    List<RegistrationPlan> findByStudentId(@Param("studentId") Integer studentId);

    @Query("select p from RegistrationPlan p where p.student.studentId = :studentId order by p.priority asc")
    List<RegistrationPlan> findByStudentIdOrderByPriorityAsc(@Param("studentId") Integer studentId);

    @Query("select count(p) from RegistrationPlan p where p.student.studentId = :studentId")
    long countByStudentId(@Param("studentId") Integer studentId);
}
