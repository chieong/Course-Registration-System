package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationPeriodRepositoryPort {
    Optional<RegistrationPeriod> findById(Integer id);
    List<RegistrationPeriod> findAll();
    RegistrationPeriod save(RegistrationPeriod period);
    Optional<RegistrationPeriod> findActivePeriod(int cohort, LocalDateTime now);
}
