package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativeCourseRestControllerTest {

    @Mock
    private AdministrativeService administrativeService;

    @Mock
    private CourseService courseService;

    @Mock
    private RegistrationRecordRepository registrationRecordRepository;

    @Mock
    private WaitlistRecordRepository waitlistRecordRepository;

    @InjectMocks
    private AdministrativeCourseRestController controller;

    @Test
    void listCourses_returnsSafeSummaryPayload() {
        Course course = new Course();
        course.setCourseCode("CSC302");
        course.setTitle("Data Structures II");
        course.setCredits(3);

        Section section = new Section();
        section.setSectionId(20);
        section.setType(Section.Type.LECTURE);
        section.setVenue("Room A101");
        section.setTime(LocalDateTime.of(2026, 4, 21, 9, 0), LocalDateTime.of(2026, 4, 21, 11, 0));
        section.setEnrollCapacity(30);
        section.setWaitlistCapacity(10);
        section.setCourse(course);
        course.setSections(Set.of(section));

        when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));
        when(registrationRecordRepository.countEnrolled(20)).thenReturn(18);
        when(waitlistRecordRepository.countWaitlisted(20)).thenReturn(2);

        ResponseEntity<List<AdministrativeCourseRestController.CourseSummaryResponse>> response = controller.listCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("CSC302", response.getBody().get(0).courseCode());
        assertEquals(18, response.getBody().get(0).enrolled());
        assertEquals(30, response.getBody().get(0).maxEnroll());
        assertEquals(10, response.getBody().get(0).waitlistSize());
    }
}
