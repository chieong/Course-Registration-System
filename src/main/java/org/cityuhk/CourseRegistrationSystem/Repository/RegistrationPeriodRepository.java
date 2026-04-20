package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Integer> {

    @Query("select p from RegistrationPeriod p where p.term = :term and p.cohort = :cohort and p.startDateTime <= :now and p.endDateTime >= :now")
    Optional<RegistrationPeriod> findActivePeriod(@Param("term") String term,
                                                  @Param("cohort") Integer cohort,
                                                  @Param("now") LocalDateTime now);

    @Query("select p from RegistrationPeriod p where p.term = :term and p.cohort = :cohort and p.startDateTime > :now order by p.startDateTime asc")
    List<RegistrationPeriod> findUpcomingPeriods(@Param("term") String term,
                                                 @Param("cohort") Integer cohort,
                                                 @Param("now") LocalDateTime now);

    @Query("select p from RegistrationPeriod p where p.term = :term and p.cohort = :cohort order by p.startDateTime asc")
    List<RegistrationPeriod> findByTermAndCohortOrderByStartDateTime(@Param("term") String term,
                                                                     @Param("cohort") Integer cohort);

    @Query("select p from RegistrationPeriod p where p.startDateTime <= :now and p.endDateTime >= :now")
    List<RegistrationPeriod> findActivePeriods(@Param("now") LocalDateTime now);

    @Query("select p from RegistrationPeriod p where p.term = :term order by p.endDateTime desc")
    List<RegistrationPeriod> findByTermOrderByEndDateTimeDesc(@Param("term") String term);

    @Query("select distinct p.term from RegistrationPeriod p")
    List<String> findDistinctTerms();
}
