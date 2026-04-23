package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Validator for timetable export requirements.
 * Ensures all necessary data is valid before export.
 * 
 * Applies: Single Responsibility Principle, Template Method pattern for validation steps
 */
@Component
public class TimetableValidator {
    
    private final StudentRepositoryPort studentRepository;
    private final InstructorRepositoryPort instructorRepository;
    private final RegistrationRecordRepositoryPort registrationRecordRepository;
    
    public TimetableValidator(StudentRepositoryPort studentRepository,
                            InstructorRepositoryPort instructorRepository,
                            RegistrationRecordRepositoryPort registrationRecordRepository) {
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.registrationRecordRepository = registrationRecordRepository;
    }
    
    /**
     * Validates that student exists and has registration records.
     * 
     * @param studentId the student ID to validate
     * @return the Student object if valid
     * @throws TimetableValidationException if validation fails
     */
    public Student validateStudentForExport(Integer studentId) throws TimetableValidationException {
        if (studentId == null || studentId <= 0) {
            throw new TimetableValidationException("Invalid student ID: " + studentId);
        }
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new TimetableValidationException("Student not found with ID: " + studentId));
        
        List<?> records = registrationRecordRepository.findByStudentId(studentId);
        if (records == null || records.isEmpty()) {
            throw new TimetableValidationException("Student has no registration records for timetable export");
        }
        
        return student;
    }

    public Instructor validateInstructorForExport(Integer staffId) throws TimetableValidationException {
        if (staffId == null || staffId <= 0) {
            throw new TimetableValidationException("Invalid staff ID: " + staffId);
        }

        Instructor instructor =  instructorRepository.findById(staffId).orElseThrow(() -> new TimetableValidationException("Instructor not found with ID: " + staffId));

        Set<Section> sections = instructor.getSections();
        if (sections == null || sections.isEmpty()) {
            throw new TimetableValidationException("Instructor has no sections");
        }
        return instructor;
    }
    
    /**
     * Validates that timetable data is complete.
     * 
     * @param timetableData the timetable data to validate
     * @throws TimetableValidationException if validation fails
     */
    public void validateTimetableData(TimetableData timetableData) throws TimetableValidationException {
        if (timetableData == null) {
            throw new TimetableValidationException("Timetable data cannot be null");
        }
        
        if (timetableData.getOwnerId() == null) {
            throw new TimetableValidationException("Student ID is missing from timetable data");
        }
        
        if (timetableData.getSections() == null || timetableData.getSections().isEmpty()) {
            throw new TimetableValidationException("Timetable data has no registration records");
        }
        
        if (timetableData.getDayFormatter() == null || timetableData.getTimeFormatter() == null) {
            throw new TimetableValidationException("Timetable data formatters are not configured");
        }
    }
}
