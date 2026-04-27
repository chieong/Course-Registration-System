package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationPeriodRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Integer>, RegistrationPeriodRepositoryPort {

    @Query("select p from RegistrationPeriod p where p.cohort = :cohort and p.startDateTime <= :now and p.endDateTime >= :now")
    Optional<RegistrationPeriod> findActivePeriod(@Param("cohort") Integer cohort,
                                                  @Param("now") LocalDateTime now);

    @Query("select p from RegistrationPeriod p where p.cohort = :cohort and p.startDateTime > :now order by p.startDateTime asc")
    List<RegistrationPeriod> findUpcomingPeriods(@Param("cohort") Integer cohort,
                                                 @Param("now") LocalDateTime now);

    @Query("select p from RegistrationPeriod p where p.cohort = :cohort order by p.startDateTime asc")
    List<RegistrationPeriod> findByCohortOrderByStartDateTime(@Param("cohort") Integer cohort);


    @Query("select p from RegistrationPeriod p where p.startDateTime <= :now and p.endDateTime >= :now")
    List<RegistrationPeriod> findActivePeriods(@Param("now") LocalDateTime now);



    @Query("SELECT r.cohort FROM RegistrationPeriod r WHERE :time BETWEEN r.startDateTime AND r.endDateTime")
    List<Integer> getActiveCohortByTime(LocalDateTime time);

    @Query("select p from RegistrationPeriod p where p.cohort = :cohort and p.startDateTime < :endDateTime and p.endDateTime > :startDateTime")
    List<RegistrationPeriod> findOverlappingPeriods(@Param("cohort") Integer cohort,
                                                    @Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime);

    @Query("select p from RegistrationPeriod p order by p.cohort asc, p.startDateTime asc")
    List<RegistrationPeriod> findAllOrderByCohortAndStartDateTime();
}
