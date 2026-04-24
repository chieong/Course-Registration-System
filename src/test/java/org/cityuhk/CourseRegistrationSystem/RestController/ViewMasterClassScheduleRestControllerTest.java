package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewMasterClassScheduleRestControllerTest {

    @Mock
    private CourseService courseService;

    @Mock
    private RegistrationRecordRepository registrationRecordRepository;

    @Mock
    private WaitlistRecordRepository waitlistRecordRepository;

    @InjectMocks
    private ViewMasterClassScheduleRestController controller;

    @Test
    void getAllSections_returnsCourseAndSectionCapacityDetails() {
        Course prerequisite = new Course();
        prerequisite.setCourseCode("CSC100");

        Course exclusive = new Course();
        exclusive.setCourseCode("CSC999");

        Instructor instructor = new Instructor.InstructorBuilder()
                .withStaffId(9)
                .withUserEID("i09")
                .withName("Dr. Wong")
                .withDepartment("CS")
                .build();

        Section section = new Section();
        section.setSectionId(33);
        section.setType(Section.Type.LECTURE);
        section.setVenue("Room C3");
        section.setTime(LocalDateTime.of(2026, 4, 23, 10, 0), LocalDateTime.of(2026, 4, 23, 12, 0));
        section.setEnrollCapacity(30);
        section.setWaitlistCapacity(10);
        section.setInstructors(Set.of(instructor));

        Course course = new Course();
        course.setCourseId(3);
        course.setCourseCode("CSC204");
        course.setTitle("Database Systems");
        course.setCredits(3);
        course.setDescription("Relational model and SQL.");
        course.setPrerequisiteCourses(Set.of(prerequisite));
        course.setExclusiveCourses(Set.of(exclusive));
        section.setCourse(course);
        course.setSections(Set.of(section));

        when(courseService.getAllCoursesWithAllData()).thenReturn(List.of(course));
        when(registrationRecordRepository.countEnrolled(33)).thenReturn(25);
        when(waitlistRecordRepository.countWaitlisted(33)).thenReturn(2);

        ResponseEntity<List<ViewMasterClassScheduleRestController.MasterScheduleCourseResponse>> response = controller.getAllSections();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        ViewMasterClassScheduleRestController.MasterScheduleCourseResponse courseResponse = response.getBody().get(0);
        assertEquals("CSC204", courseResponse.courseCode());
        assertEquals(List.of("CSC100"), courseResponse.prerequisites());
        assertEquals(List.of("CSC999"), courseResponse.exclusives());
        assertEquals(1, courseResponse.sections().size());

        ViewMasterClassScheduleRestController.MasterScheduleSectionResponse sectionResponse = courseResponse.sections().get(0);
        assertEquals(Integer.valueOf(33), sectionResponse.sectionId());
        assertEquals("lecture", sectionResponse.type());
        assertEquals(25, sectionResponse.enrolled());
        assertEquals(30, sectionResponse.enrollCapacity());
        assertEquals(5, sectionResponse.availableEnroll());
        assertEquals(2, sectionResponse.waitlisted());
        assertEquals(8, sectionResponse.availableWaitlist());
        assertEquals(List.of("Dr. Wong"), sectionResponse.instructors());
    }
}
