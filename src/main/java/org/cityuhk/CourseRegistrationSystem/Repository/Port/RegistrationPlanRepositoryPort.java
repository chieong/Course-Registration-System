package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import java.util.List;
import java.util.Optional;

public interface RegistrationPlanRepositoryPort {
    Optional<RegistrationPlan> findById(Integer id);
    List<RegistrationPlan> findByStudentId(Integer studentId);
    List<RegistrationPlan> findByStudentIdOrderByPriorityAsc(Integer studentId);
    long countByStudentId(Integer studentId);
    RegistrationPlan save(RegistrationPlan plan);
    void deleteById(Integer id);
}
