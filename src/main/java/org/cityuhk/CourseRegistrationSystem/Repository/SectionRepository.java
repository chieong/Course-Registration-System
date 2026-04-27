package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Integer>, SectionRepositoryPort {
    @Query("select s from Section s")
    public List<Section> findAll();

    @Query(
            "select case when count(s) > 0 then true else false end from Section s"
                    + " where s.venue = :venue and s.startTime < :endTime and s.endTime > :startTime")
    boolean overlapsInVenue(
            @Param("venue") String venue,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
