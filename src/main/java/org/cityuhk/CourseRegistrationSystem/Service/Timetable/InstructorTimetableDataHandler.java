package org.cityuhk.CourseRegistrationSystem.Service.Timetable;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class InstructorTimetableDataHandler implements TimetableDataHandler {

    private final TimetableValidator validator;
    private final InstructorRepository instructorRepository;

    public InstructorTimetableDataHandler(TimetableValidator validator,
                                            InstructorRepository instructorRepository) {
        this.validator = validator;
        this.instructorRepository = instructorRepository;
    }

    @Override
    public TimetableData.UserType userType() {
        return TimetableData.UserType.Instructor;
    }

    @Override
    public String ownerIdLabel() {
        return "Staff ID";
    }

    @Override
    public void validateForExport(Integer ownerId) throws TimetableValidationException {
        validator.validateInstructorForExport(ownerId);
    }

    @Override
    public Set<Section> loadSections(Integer ownerId) throws TimetableValidationException {
        Optional<Instructor> instructor = instructorRepository.findById(ownerId);
        if (instructor.isEmpty()) {
            throw new TimetableValidationException("Instructor does not exist");
        }
        return instructor.get().getSections();
    }
}
