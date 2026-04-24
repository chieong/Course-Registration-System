package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminCourseRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdministrativeCourseRestController {

    private final AdministrativeService administrativeService;
    private final CourseService courseService;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final WaitlistRecordRepository waitlistRecordRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AdministrativeCourseRestController(AdministrativeService administrativeService,
                                              CourseService courseService,
                                              RegistrationRecordRepository registrationRecordRepository,
                                              WaitlistRecordRepository waitlistRecordRepository) {
        this.administrativeService = administrativeService;
        this.courseService = courseService;
        this.registrationRecordRepository = registrationRecordRepository;
        this.waitlistRecordRepository = waitlistRecordRepository;
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseSummaryResponse>> listCourses() {
        List<CourseSummaryResponse> response = courseService.getAllCoursesWithAllData().stream()
                .sorted(Comparator.comparing(Course::getCourseCode, String.CASE_INSENSITIVE_ORDER))
                .map(this::toSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseSummaryResponse> createCourse(@RequestBody AdminCourseRequest request) {
        Course created = administrativeService.createCourse(request);
        return ResponseEntity.ok(loadMappedByCode(created.getCourseCode()));
    }

    @PutMapping("/course")
    public ResponseEntity<CourseSummaryResponse> modifyCourse(@RequestBody AdminCourseRequest request) {
        Course update = administrativeService.modifyCourse(request);
        return ResponseEntity.ok(loadMappedByCode(update.getCourseCode()));
    }

    @DeleteMapping("/course/{courseCode}")
    public ResponseEntity<Void> removeCourse(@PathVariable String courseCode) {
        administrativeService.removeCourse(courseCode);
        return ResponseEntity.noContent().build();
    }

    private CourseSummaryResponse loadMappedByCode(String courseCode) {
        return courseService.getAllCoursesWithAllData().stream()
                .filter(course -> courseCode.equalsIgnoreCase(course.getCourseCode()))
                .findFirst()
                .map(this::toSummary)
                .orElse(new CourseSummaryResponse(
                        courseCode,
                        "-",
                        null,
                        0,
                        "Core",
                        "Computer Science",
                        "TBA",
                        "00:00",
                        "00:00",
                        "TBA",
                        0,
                        0,
                        0,
                        0
                ));
    }

    private CourseSummaryResponse toSummary(Course course) {
        Section representative = safeSections(course.getSections()).stream()
                .sorted(Comparator.comparing(Section::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .findFirst()
                .orElse(null);

        String day = "TBA";
        String startTime = "00:00";
        String endTime = "00:00";
        String location = "TBA";
        String category = "Core";
        String dept = "Computer Science";
        int enrolled = 0;
        int maxEnroll = 0;
        int waitlistSize = 0;
        int waitlistCurrent = 0;

        if (representative != null) {
            LocalDateTime start = representative.getStartTime();
            LocalDateTime end = representative.getEndTime();

            if (start != null) {
                day = start.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                startTime = start.format(TIME_FORMATTER);
            }
            if (end != null) {
                endTime = end.format(TIME_FORMATTER);
            }

            location = representative.getVenue() == null || representative.getVenue().isBlank()
                    ? "TBA"
                    : representative.getVenue();

            if (representative.getType() == Section.Type.LAB) {
                category = "Lab";
            } else if (representative.getType() == Section.Type.TUTORIAL) {
                category = "Elective";
            }

            dept = representative.getInstructors() == null
                    ? dept
                    : representative.getInstructors().stream()
                    .map(instructor -> instructor.getDepartment())
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst()
                    .orElse(dept);

            maxEnroll = representative.getEnrollCapacity() == null ? 0 : representative.getEnrollCapacity();
            waitlistSize = representative.getWaitlistCapacity() == null ? 0 : representative.getWaitlistCapacity();
            enrolled = registrationRecordRepository.countEnrolled(representative.getSectionId());
            waitlistCurrent = waitlistRecordRepository.countWaitlisted(representative.getSectionId());
        }

        return new CourseSummaryResponse(
                course.getCourseCode(),
                course.getTitle(),
                course.getDescription(),
                course.getCredits(),
                category,
                dept,
                day,
                startTime,
                endTime,
                location,
                enrolled,
                maxEnroll,
                waitlistSize,
                waitlistCurrent
        );
    }

    private static Set<Section> safeSections(Set<Section> sections) {
        return sections == null ? Set.of() : sections;
    }

    public record CourseSummaryResponse(
            String courseCode,
            String title,
            String description,
            int credits,
            String category,
            String dept,
            String day,
            String startTime,
            String endTime,
            String location,
            int enrolled,
            int maxEnroll,
            int waitlistSize,
            int waitlistCurrent
    ) {
    }
}
