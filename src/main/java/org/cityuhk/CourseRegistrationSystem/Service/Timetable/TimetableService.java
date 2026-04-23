package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

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
    
    private final RegistrationRecordRepositoryPort registrationRecordRepository;
    private final InstructorRepository instructorRepository;
    private final TimetableValidator validator;
    private final TimetableExporter defaultExporter;
    
    public TimetableService(RegistrationRecordRepositoryPort registrationRecordRepository,
                           InstructorRepository instructorRepository,
                           TimetableValidator validator,
                           TextTimetableExporter defaultExporter) {
        this.registrationRecordRepository = registrationRecordRepository;
        this.instructorRepository = instructorRepository;
        this.validator = validator;
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
        Student student = validator.validateStudentForExport(studentId);
        
        // Step 2: Build timetable data
        TimetableData timetableData = buildStudentTimetableData(studentId);
        
        // Step 3: Validate built data
        validator.validateTimetableData(timetableData);
        
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
        Student student = validator.validateStudentForExport(studentId);
        
        // Build timetable data with custom formatters
        TimetableData timetableData = new TimetableData.Builder()
            .ownerId(studentId)
            .userType(TimetableData.UserType.Student)
            .dayFormatter(dayFormatter)
            .timeFormatter(timeFormatter)
            .build();
        
        // Validate and export
        validator.validateTimetableData(timetableData);
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
    public TimetableData getStudentTimetableData(Integer studentId) throws TimetableValidationException {
        return buildStudentTimetableData(studentId);
    }

    public TimetableData getInstructorTimetableData(Integer staffId) throws TimetableValidationException {
        return buildInstructorTimetableData(staffId);
    }
    
    /**
     * Builds timetable data for a student.
     * Internal method that coordinates data fetching and structuring.
     * 
     * @param studentId the ID of the student
     * @return the constructed TimetableData
     * @throws TimetableValidationException if build fails
     */
    private TimetableData buildStudentTimetableData(Integer studentId) throws TimetableValidationException {
        try {
            return new TimetableData.Builder()
                    .ownerId(studentId)
                    .userType(TimetableData.UserType.Student)
                    .build();
        } catch (IllegalStateException ex) {
            throw new TimetableValidationException("Failed to build timetable data: " + ex.getMessage());
        }
    }

    private TimetableData buildInstructorTimetableData(Integer staffId) throws TimetableValidationException {
        try{
            Optional<Instructor> instructor = instructorRepository.findById(staffId);
            if(instructor.isEmpty()) {
                throw new TimetableValidationException("Instructor does not exist");
            }
            Set<Section> sections = instructor.get().getSections();
            return new TimetableData.Builder()
                    .ownerId(staffId)
                    .userType(TimetableData.UserType.Instructor)
                    .sections(sections)
                    .build();
        } catch (IllegalStateException ex) {
            throw new TimetableValidationException("Failed to build timetable data: " + ex.getMessage());
        }
    }
}
