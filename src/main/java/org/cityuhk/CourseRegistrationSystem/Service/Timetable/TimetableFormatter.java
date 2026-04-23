package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Section;

public interface TimetableFormatter {

    String formatTitle(TimetableData timetableData);

    String formatHeader();

    String formatRow(Section section, TimetableData timetableData);
}