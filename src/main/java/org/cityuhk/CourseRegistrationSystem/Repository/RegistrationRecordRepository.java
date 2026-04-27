package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationRecordRepository extends JpaRepository<RegistrationRecord, Integer>, RegistrationRecordRepositoryPort {

    @Query("select count(e) from RegistrationRecord e where e.section.sectionId = :sectionId")
    int countEnrolled(@Param("sectionId") Integer sectionId);

    @Query("select r from RegistrationRecord r")
       List<RegistrationRecord> findAllRecords();
       
    @Query("select case when count(e) > 0 then true else false end from RegistrationRecord e " +
           "where e.student.studentId = :studentId and e.section.sectionId = :sectionId")
    boolean exists(@Param("studentId") Integer studentId, @Param("sectionId") Integer sectionId);
    
    @Query("select e from RegistrationRecord e " +
            "where e.student.studentId = :studentId and e.section.sectionId = :sectionId")
    Optional<RegistrationRecord> findByStudentIdAndSectionId(@Param("studentId") Integer studentId, @Param("sectionId") Integer sectionId);

    @Query("select e from RegistrationRecord e " +
           "where e.student.studentId = :studentId and e.timestamp between :start and :end")
    List<RegistrationRecord> find(@Param("studentId") Integer studentId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("select e from RegistrationRecord e where e.student.studentId = :studentId order by e.timestamp asc")
    List<RegistrationRecord> findByStudentId(@Param("studentId") Integer studentId);

    @Query("select e from RegistrationRecord e where e.section.sectionId = :sectionId order by e.timestamp asc")
    List<RegistrationRecord> findBySectionId(@Param("sectionId") Integer sectionId);

    @Query("""
      SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
      FROM RegistrationRecord e
      WHERE e.section.course.courseCode = :courseCode
    """)
    boolean existsByCourseCode(@Param("courseCode") String courseCode);
}

