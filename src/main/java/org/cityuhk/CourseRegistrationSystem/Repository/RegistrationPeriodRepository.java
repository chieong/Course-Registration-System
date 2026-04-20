package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Integer> {
    @Query("SELECT r.cohort FROM RegistrationPeriod r WHERE :time BETWEEN r.startDateTime AND r.endDateTime")
    List<Integer> getActiveCohortByTime(LocalDateTime time);
}
