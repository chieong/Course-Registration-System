package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

public class TimetableExportException extends Exception {

    public TimetableExportException(String message) {
        super(message);
    }

    public TimetableExportException(String message, Throwable cause) {
        super(message, cause);
    }
}