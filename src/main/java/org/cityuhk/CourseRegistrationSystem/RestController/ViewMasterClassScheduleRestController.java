package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Service.Academic.CourseService;
import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/sections")
public class ViewMasterClassScheduleRestController {
    private final CourseService courseService;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final WaitlistRecordRepository waitlistRecordRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    public ViewMasterClassScheduleRestController(CourseService courseService,
                         RegistrationRecordRepository registrationRecordRepository,
                         WaitlistRecordRepository waitlistRecordRepository) {
        this.courseService = courseService;
    this.registrationRecordRepository = registrationRecordRepository;
    this.waitlistRecordRepository = waitlistRecordRepository;
    }

    @GetMapping
    public ResponseEntity<List<MasterScheduleCourseResponse>> getAllSections() {
    List<MasterScheduleCourseResponse> response = courseService.getAllCoursesWithAllData().stream()
        .sorted(Comparator.comparing(Course::getCourseCode, String.CASE_INSENSITIVE_ORDER))
        .map(this::toCourseResponse)
        .toList();

    return ResponseEntity.ok(response);
    }

    private MasterScheduleCourseResponse toCourseResponse(Course course) {
    List<String> prerequisites = safeSet(course.getPrerequisiteCourses()).stream()
        .map(Course::getCourseCode)
        .filter(code -> code != null && !code.isBlank())
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    List<String> exclusives = safeSet(course.getExclusiveCourses()).stream()
        .map(Course::getCourseCode)
        .filter(code -> code != null && !code.isBlank())
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    List<MasterScheduleSectionResponse> sections = safeSet(course.getSections()).stream()
        .sorted(Comparator.comparing(Section::getStartTime))
        .map(this::toSectionResponse)
        .toList();

    return new MasterScheduleCourseResponse(
        course.getCourseId(),
        course.getCourseCode(),
        course.getTitle(),
        course.getCredits(),
        course.getDescription(),
        prerequisites,
        exclusives,
        sections
    );
    }

    private MasterScheduleSectionResponse toSectionResponse(Section section) {
    int enrolled = registrationRecordRepository.countEnrolled(section.getSectionId());
    int waitlisted = waitlistRecordRepository.countWaitlisted(section.getSectionId());
    int enrollCapacity = section.getEnrollCapacity() == null ? 0 : section.getEnrollCapacity();
    int waitlistCapacity = section.getWaitlistCapacity() == null ? 0 : section.getWaitlistCapacity();

    List<String> instructors = safeSet(section.getInstructors()).stream()
        .map(instructor -> instructor.getUserName() == null || instructor.getUserName().isBlank()
            ? instructor.getUserEID()
            : instructor.getUserName())
        .filter(name -> name != null && !name.isBlank())
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .toList();

    return new MasterScheduleSectionResponse(
        section.getSectionId(),
        section.getType() == null ? "-" : section.getType().name().toLowerCase(Locale.ROOT),
        section.getStartTime() == null ? "-" : section.getStartTime().format(DATE_TIME_FORMATTER),
        section.getEndTime() == null ? "-" : section.getEndTime().format(DATE_TIME_FORMATTER),
        section.getVenue(),
        instructors,
        enrolled,
        enrollCapacity,
        Math.max(0, enrollCapacity - enrolled),
        waitlisted,
        waitlistCapacity,
        Math.max(0, waitlistCapacity - waitlisted)
    );
    }

    private static <T> Set<T> safeSet(Set<T> source) {
    return source == null ? Set.of() : source;
    }

    public record MasterScheduleCourseResponse(
        Integer courseId,
        String courseCode,
        String title,
        int credits,
        String description,
        List<String> prerequisites,
        List<String> exclusives,
        List<MasterScheduleSectionResponse> sections
    ) {
    }

    public record MasterScheduleSectionResponse(
        Integer sectionId,
        String type,
        String startTime,
        String endTime,
        String venue,
        List<String> instructors,
        int enrolled,
        int enrollCapacity,
        int availableEnroll,
        int waitlisted,
        int waitlistCapacity,
        int availableWaitlist
    ) {
    }

}

