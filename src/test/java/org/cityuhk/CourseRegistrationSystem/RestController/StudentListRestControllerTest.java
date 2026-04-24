package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentListRestControllerTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private RegistrationRecordRepository registrationRecordRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private StudentListRestController controller;

    @Test
    void getStudentList_instructorView_returnsSectionGroups() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("inst01");

        Instructor instructor = new Instructor.InstructorBuilder()
                .withStaffId(11)
                .withUserEID("inst01")
                .withName("Dr. Lim")
                .withDepartment("CS")
                .build();

        Course course = new Course();
        course.setCourseCode("CSC204");
        course.setTitle("Database Systems");

        Section section = new Section();
        section.setSectionId(5);
        section.setCourse(course);
        section.setType(Section.Type.LECTURE);
        section.setVenue("Room C3");
        section.setTime(LocalDateTime.of(2026, 4, 21, 10, 0), LocalDateTime.of(2026, 4, 21, 12, 0));
        instructor.setSections(Set.of(section));

        Student student = new Student.StudentBuilder()
                .withStudentId(1001)
                .withUserEID("s1001")
                .withName("Alice")
                .withMajor("Computer Science")
                .withCohort(2024)
                .build();

        RegistrationRecord record = new RegistrationRecord(student, section, LocalDateTime.now());

        when(adminRepository.findByUserEID("inst01")).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEIDWithSections("inst01")).thenReturn(Optional.of(instructor));
        when(registrationRecordRepository.findBySectionId(5)).thenReturn(List.of(record));

        ResponseEntity<?> response = controller.getStudentList(authentication, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());

        @SuppressWarnings("unchecked")
        List<StudentListRestController.SectionStudentListResponse> body =
                (List<StudentListRestController.SectionStudentListResponse>) response.getBody();

        assertEquals(1, body.size());
        assertEquals("CSC204", body.get(0).code());
        assertEquals(1, body.get(0).students().size());
        assertEquals("s1001", body.get(0).students().get(0).id());
    }

    @Test
    void getStudentList_instructorCannotReadOthers_returns403() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("inst01");
        when(adminRepository.findByUserEID("inst01")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getStudentList(authentication, "anotherInst");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
