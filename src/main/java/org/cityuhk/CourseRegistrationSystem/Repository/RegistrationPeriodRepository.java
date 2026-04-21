package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationPeriodRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Integer>, RegistrationPeriodRepositoryPort {

    @Query("select rp from RegistrationPeriod rp where rp.cohort = :cohort " +
           "and rp.startDateTime <= :now and rp.endDateTime >= :now")
    Optional<RegistrationPeriod> findActivePeriod(@Param("cohort") int cohort, @Param("now") LocalDateTime now);
}
