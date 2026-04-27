package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Service
public class TimetableService {

    private final TimetableValidator validator;
    private final TimetableExporter defaultExporter;

    private final TimetableDataHandler studentOwnerProvider;
    private final TimetableDataHandler instructorOwnerProvider;

    public TimetableService(TimetableValidator validator,
                            TextTimetableExporter defaultExporter,
                            StudentTimetableDataHandler studentOwnerProvider,
                            InstructorTimetableDataHandler instructorOwnerProvider) {
        this.validator = validator;
        this.defaultExporter = defaultExporter;
        this.studentOwnerProvider = studentOwnerProvider;
        this.instructorOwnerProvider = instructorOwnerProvider;
    }

    public String getStudentTimetableString(Integer studentId) throws TimetableExportException, TimetableValidationException{
        TimetableData data = buildTimetableData(studentId, studentOwnerProvider, null, null);
        validator.validateTimetableData(data);
        return defaultExporter.print(data);
    }

    public String getInstructorTimetableString(Integer studentId) throws TimetableExportException, TimetableValidationException{
        TimetableData data = buildTimetableData(studentId, instructorOwnerProvider, null, null);
        validator.validateTimetableData(data);
        return defaultExporter.print(data);
    }

    public Path exportStudentTimetable(Integer studentId) throws TimetableExportException, TimetableValidationException {
        return exportTimetable(studentId, studentOwnerProvider, defaultExporter);
    }

    public Path exportStudentTimetable(Integer studentId, TimetableExporter exporter)
            throws TimetableExportException, TimetableValidationException {
        return exportTimetable(studentId, studentOwnerProvider, exporter);
    }

    public Path exportStudentTimetableWithFormatters(Integer studentId,
                                                     DateTimeFormatter dayFormatter,
                                                     DateTimeFormatter timeFormatter)
            throws TimetableExportException, TimetableValidationException {

        TimetableData data = buildTimetableData(studentId, studentOwnerProvider, dayFormatter, timeFormatter);
        validator.validateTimetableData(data);
        return defaultExporter.export(data);
    }

    public TimetableData getStudentTimetableData(Integer studentId) throws TimetableValidationException {
        return buildTimetableData(studentId, studentOwnerProvider, null, null);
    }

    public Path exportInstructorTimetable(Integer staffId) throws TimetableExportException, TimetableValidationException {
        return exportTimetable(staffId, instructorOwnerProvider, defaultExporter);
    }

    public Path exportInstructorTimetable(Integer staffId, TimetableExporter exporter)
            throws TimetableExportException, TimetableValidationException {
        return exportTimetable(staffId, instructorOwnerProvider, exporter);
    }

    public TimetableData getInstructorTimetableData(Integer staffId) throws TimetableValidationException {
        return buildTimetableData(staffId, instructorOwnerProvider, null, null);
    }

    public Path exportTimetable(Integer ownerId,
                                TimetableDataHandler ownerProvider,
                                TimetableExporter exporter)
            throws TimetableExportException, TimetableValidationException {

        if (ownerProvider == null) {
            throw new TimetableValidationException("Owner provider cannot be null");
        }
        if (exporter == null) {
            throw new TimetableExportException("Exporter cannot be null");
        }

        TimetableData data = buildTimetableData(ownerId, ownerProvider, null, null);

        validator.validateTimetableData(data);
        return exporter.export(data);
    }

    private TimetableData buildTimetableData(Integer ownerId,
                                             TimetableDataHandler ownerProvider,
                                             DateTimeFormatter dayFormatter,
                                             DateTimeFormatter timeFormatter)
            throws TimetableValidationException {

        ownerProvider.validateForExport(ownerId);

        TimetableData.Builder builder = new TimetableData.Builder()
                .ownerId(ownerId)
                .ownerIdLabel(ownerProvider.ownerIdLabel())
                .userType(ownerProvider.userType())
                .sections(ownerProvider.loadSections(ownerId));

        if (dayFormatter != null) {
            builder.dayFormatter(dayFormatter);
        }
        if (timeFormatter != null) {
            builder.timeFormatter(timeFormatter);
        }

        return builder.build();
    }
}
