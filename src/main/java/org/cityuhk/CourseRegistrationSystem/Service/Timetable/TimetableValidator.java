package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator for timetable export requirements.
 * Ensures all necessary data is valid before export.
 * 
 * Applies: Single Responsibility Principle, Template Method pattern for validation steps
 */
@Component
public class TimetableValidator {
    
    private final StudentRepository studentRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    
    public TimetableValidator(StudentRepository studentRepository, 
                            RegistrationRecordRepository registrationRecordRepository) {
        this.studentRepository = studentRepository;
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
        
        if (timetableData.getStudentId() == null) {
            throw new TimetableValidationException("Student ID is missing from timetable data");
        }
        
        if (timetableData.getRegistrationRecords() == null || timetableData.getRegistrationRecords().isEmpty()) {
            throw new TimetableValidationException("Timetable data has no registration records");
        }
        
        if (timetableData.getDayFormatter() == null || timetableData.getTimeFormatter() == null) {
            throw new TimetableValidationException("Timetable data formatters are not configured");
        }
    }
}
