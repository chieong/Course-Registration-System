package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimetableRestControllerTest {

    @Mock
    private TimetableService timetableService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @InjectMocks
    private TimetableRestController controller;

    @Test
    void getCurrentUserTimetable_studentSuccess_returnsMappedResponse() throws TimetableValidationException {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("s123");

        Student student = new Student.StudentBuilder()
                .withStudentId(11)
                .withUserEID("s123")
                .withName("Alice Chan")
                .withMajor("Computer Science")
                .build();

        Course course = new Course();
        course.setCourseId(50);
        course.setCourseCode("CSC204");
        course.setTitle("Database Systems");
        course.setCredits(3);

        Section section = new Section();
        section.setSectionId(101);
        section.setCourse(course);
        section.setType(Section.Type.LECTURE);
        section.setVenue("Room B2");
        section.setTime(LocalDateTime.of(2026, 4, 20, 9, 0), LocalDateTime.of(2026, 4, 20, 11, 0));

        TimetableData timetableData = new TimetableData.Builder()
                .ownerId(11)
                .userType(TimetableData.UserType.Student)
                .sections(Set.of(section))
                .build();

        when(studentRepository.findByUserEID("s123")).thenReturn(Optional.of(student));
        when(timetableService.getStudentTimetableData(11)).thenReturn(timetableData);

        ResponseEntity<?> response = controller.getCurrentUserTimetable(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Object body = response.getBody();
        assertInstanceOf(TimetableRestController.TimetableResponse.class, body);

        TimetableRestController.TimetableResponse data = (TimetableRestController.TimetableResponse) body;
        assertEquals("STUDENT", data.role());
        assertEquals("Alice Chan", data.displayName());
        assertEquals("Computer Science", data.programme());
        assertEquals(1, data.totalCourses());
        assertEquals("M", data.entries().get(0).day());
        assertEquals("09:00", data.entries().get(0).start());
        assertEquals("11:00", data.entries().get(0).end());
    }

    @Test
    void getCurrentUserTimetable_userMissing_returnsBadRequest() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("unknown");

        when(studentRepository.findByUserEID("unknown")).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID("unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getCurrentUserTimetable(authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unsupported timetable user role", response.getBody());
    }
}
