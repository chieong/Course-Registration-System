package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationPeriodRepositoryPort {
    Optional<RegistrationPeriod> findById(Integer id);
    List<RegistrationPeriod> findAll();
    RegistrationPeriod save(RegistrationPeriod period);

    Optional<RegistrationPeriod> findActivePeriod(Integer cohort, LocalDateTime now);
    List<RegistrationPeriod> findUpcomingPeriods(Integer cohort, LocalDateTime now);
    List<RegistrationPeriod> findByCohortOrderByStartDateTime(Integer cohort);
    List<RegistrationPeriod> findActivePeriods(LocalDateTime now);
    List<Integer> getActiveCohortByTime(LocalDateTime time);
    List<RegistrationPeriod> findOverlappingPeriods(Integer cohort, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<RegistrationPeriod> findAllOrderByCohortAndStartDateTime();
    void deleteById(Integer id);
    boolean existsById(Integer id);
}