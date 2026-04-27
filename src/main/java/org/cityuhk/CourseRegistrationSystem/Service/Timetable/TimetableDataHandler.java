package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Section;

import java.util.Set;

public interface TimetableDataHandler {

    TimetableData.UserType userType();

    String ownerIdLabel();

    void validateForExport(Integer ownerId) throws TimetableValidationException;

    Set<Section> loadSections(Integer ownerId) throws TimetableValidationException;
}
