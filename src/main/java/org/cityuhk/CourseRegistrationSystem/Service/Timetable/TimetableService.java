package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing timetable operations.
 * Acts as a Facade orchestrating multiple components for timetable export and management.
 * 
 * Applies: Facade Pattern, Single Responsibility Principle, Dependency Inversion
 * - Coordinates validation, data building, and export
 * - Depends on abstractions (TimetableExporter) for extensibility
 * - Decouples controllers from complex timetable logic
 */
@Service
public class TimetableService {
    
    private final StudentRepository studentRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final TimetableExporter defaultExporter;
    
    public TimetableService(StudentRepository studentRepository,
                           RegistrationRecordRepository registrationRecordRepository,
                           TextTimetableExporter defaultExporter) {
        this.studentRepository = studentRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.defaultExporter = defaultExporter;
    }
    
    /**
     * Exports a student's timetable to a file using the default (text) format.
     * 
     * @param studentId the ID of the student
     * @return the path to the exported file
     * @throws TimetableExportException if export fails
     * @throws TimetableValidationException if validation fails
     */
    public Path exportTimetable(Integer studentId) throws TimetableExportException, TimetableValidationException {
        return exportTimetable(studentId, defaultExporter);
    }
    
    /**
     * Exports a student's timetable to a file using a specific exporter.
     * 
     * @param studentId the ID of the student
     * @param exporter the TimetableExporter to use
     * @return the path to the exported file
     * @throws TimetableExportException if export fails
     * @throws TimetableValidationException if validation fails
     */
    public Path exportTimetable(Integer studentId, TimetableExporter exporter) 
            throws TimetableExportException, TimetableValidationException {
        if (exporter == null) {
            throw new TimetableExportException("Exporter cannot be null");
        }
        
        // Step 1: Validate student and retrieve data
        validateStudentForExport(studentId);
        
        // Step 2: Build timetable data
        TimetableData timetableData = buildTimetableData(studentId);
        
        // Step 3: Validate built data
        validateTimetableData(timetableData);
        
        // Step 4: Export using the provided exporter
        return exporter.export(timetableData);
    }
    
    /**
     * Exports a student's timetable with custom formatters.
     * 
     * @param studentId the ID of the student
     * @param dayFormatter the formatter for day names (e.g., "EEE")
     * @param timeFormatter the formatter for times (e.g., "HH:mm")
     * @return the path to the exported file
     * @throws TimetableExportException if export fails
     * @throws TimetableValidationException if validation fails
     */
    public Path exportTimetableWithFormatters(Integer studentId, 
                                             DateTimeFormatter dayFormatter,
                                             DateTimeFormatter timeFormatter) 
            throws TimetableExportException, TimetableValidationException {
        // Validate student
        Student student = validateStudentForExport(studentId);
        
        // Build timetable data with custom formatters
        TimetableData timetableData = new TimetableData.Builder()
            .studentId(studentId)
            .registrationRecords(registrationRecordRepository.findByStudentId(studentId))
            .dayFormatter(dayFormatter)
            .timeFormatter(timeFormatter)
            .build();
        
        // Validate and export
        validateTimetableData(timetableData);
        return defaultExporter.export(timetableData);
    }
    
    /**
     * Retrieves information about a student's timetable without exporting.
     * Useful for checking timetable status before export.
     * 
     * @param studentId the ID of the student
     * @return the timetable data
     * @throws TimetableValidationException if validation fails
     */
    public TimetableData getTimetableData(Integer studentId) throws TimetableValidationException {
        return buildTimetableData(studentId);
    }
    
    /**
     * Builds timetable data for a student.
     * Internal method that coordinates data fetching and structuring.
     * 
     * @param studentId the ID of the student
     * @return the constructed TimetableData
     * @throws TimetableValidationException if build fails
     */
    private TimetableData buildTimetableData(Integer studentId) throws TimetableValidationException {
        try {
            return new TimetableData.Builder()
                .studentId(studentId)
                .registrationRecords(registrationRecordRepository.findByStudentId(studentId))
                .build();
        } catch (IllegalStateException ex) {
            throw new TimetableValidationException("Failed to build timetable data: " + ex.getMessage());
        }
    }

    private Student validateStudentForExport(Integer studentId) throws TimetableValidationException {
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

    private void validateTimetableData(TimetableData timetableData) throws TimetableValidationException {
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
