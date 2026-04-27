package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationRecordRepositoryPort {
    int countEnrolled(Integer sectionId);
    List<RegistrationRecord> findAllRecords();
    boolean exists(Integer studentId, Integer sectionId);
    Optional<RegistrationRecord> findByStudentIdAndSectionId(Integer studentId, Integer sectionId);
    List<RegistrationRecord> find(Integer studentId, LocalDateTime start, LocalDateTime end);
    List<RegistrationRecord> findByStudentId(Integer studentId);
    RegistrationRecord save(RegistrationRecord record);
    void delete(RegistrationRecord record);
    List<RegistrationRecord> findBySectionId(Integer sectionId);
    boolean existsByCourseCode(String courseCode);
}
