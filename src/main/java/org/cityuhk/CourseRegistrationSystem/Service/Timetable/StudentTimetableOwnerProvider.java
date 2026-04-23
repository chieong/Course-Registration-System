package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class StudentTimetableOwnerProvider implements TimetableOwnerProvider {

    private final TimetableValidator validator;
    private final RegistrationRecordRepositoryPort registrationRecordRepository;

    public StudentTimetableOwnerProvider(TimetableValidator validator,
                                         RegistrationRecordRepositoryPort registrationRecordRepository) {
        this.validator = validator;
        this.registrationRecordRepository = registrationRecordRepository;
    }

    @Override
    public TimetableData.UserType userType() {
        return TimetableData.UserType.Student;
    }

    @Override
    public String ownerIdLabel() {
        return "Student ID";
    }

    @Override
    public void validateForExport(Integer ownerId) throws TimetableValidationException {
        validator.validateStudentForExport(ownerId);
    }

    @Override
    public Set<Section> loadSections(Integer ownerId) throws TimetableValidationException {
        Set<Section> sections = new HashSet<>();
        for (RegistrationRecord record : registrationRecordRepository.findByStudentId(ownerId)) {
            if (record != null && record.getSection() != null) {
                sections.add(record.getSection());
            }
        }
        return sections;
    }
}