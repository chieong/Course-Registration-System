package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import org.cityuhk.CourseRegistrationSystem.Model.*;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.*;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.studentListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class StudentListService {

    private final InstructorRepositoryPort instructorRepository;
    private final RegistrationRecordRepositoryPort registrationRecordRepository;


    @Autowired
    public StudentListService(InstructorRepositoryPort instructorRepository, RegistrationRecordRepositoryPort registrationRecordRepository) {
        this.instructorRepository = instructorRepository;
        this.registrationRecordRepository = registrationRecordRepository;
    }

    public List<studentListResponse> getStudentListByInstructorEID(String instructorEID) {
        Optional<Instructor> instructor = instructorRepository.findByUserEID(instructorEID);
        if(instructor.isEmpty()) {
            throw new RuntimeException("Instructor not found");
        }

        Set<Section> sections = instructor.get().getSections();
        if(sections.isEmpty()) {
            throw new RuntimeException("Section not found");
        }
        ArrayList<RegistrationRecord> enrollmentRecords = new ArrayList<>();
        for (Section section : sections) {
            List<RegistrationRecord> registrationRecord = registrationRecordRepository.findBySectionId(section.getSectionId());
            enrollmentRecords.addAll(registrationRecord);
        }

        List<studentListResponse> studentListResponse = new ArrayList<>();

        for(RegistrationRecord registrationRecord : enrollmentRecords) {
            Student student = registrationRecord.getStudent();
            Course course = registrationRecord.getCourse();
            Section section = registrationRecord.getSection();
            studentListResponse.add(new studentListResponse(student,section,course));
        }
        return studentListResponse;
    }
}