package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistrationRecordRepository extends JpaRepository<RegistrationRecord, Integer> {
    @Query(
            """
            select count(e)
            from ReigstrationRecord e
            where e.section.id = :sectionId
            """)
    int countEnrolled(Integer sectionId);

    boolean exists(Integer studentId, Integer sectionId, LocalDateTime timestamp);

    @Query(
            """
                select e
                from ReigstrationRecord e
                where e.student.id = :studentId
                  and e.submissionTime between :start and :end
            """)
    List<RegistrationRecord> find(
            Integer studentId, LocalDateTime start, LocalDateTime end);
}
