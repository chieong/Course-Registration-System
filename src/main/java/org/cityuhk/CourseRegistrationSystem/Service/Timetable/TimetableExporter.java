package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import java.nio.file.Path;


public interface TimetableExporter {

    Path export(TimetableData timetableData) throws TimetableExportException;

    String getFileExtension();

    String getFormatName();
}