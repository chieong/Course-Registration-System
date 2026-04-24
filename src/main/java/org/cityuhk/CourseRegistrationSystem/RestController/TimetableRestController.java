package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableData;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableService;
import org.cityuhk.CourseRegistrationSystem.Service.Timetable.TimetableValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/timetable")
public class TimetableRestController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TimetableService timetableService;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public TimetableRestController(TimetableService timetableService,
                                   StudentRepository studentRepository,
                                   InstructorRepository instructorRepository) {
        this.timetableService = timetableService;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserTimetable(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String userEid = authentication.getName();

        Student student = studentRepository.findByUserEID(userEid).orElse(null);
        if (student != null) {
            return buildStudentResponse(student);
        }

        Instructor instructor = instructorRepository.findByUserEID(userEid).orElse(null);
        if (instructor != null) {
            return buildInstructorResponse(instructor);
        }

        return ResponseEntity.badRequest().body("Unsupported timetable user role");
    }

    private ResponseEntity<?> buildStudentResponse(Student student) {
        try {
            TimetableData data = timetableService.getStudentTimetableData(student.getStudentId());
            return ResponseEntity.ok(toResponse(data, "STUDENT", student.getUserName(), student.getMajor()));
        } catch (TimetableValidationException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private ResponseEntity<?> buildInstructorResponse(Instructor instructor) {
        try {
            TimetableData data = timetableService.getInstructorTimetableData(instructor.getStaffId());
            return ResponseEntity.ok(toResponse(data, "INSTRUCTOR", instructor.getUserName(), instructor.getDepartment()));
        } catch (TimetableValidationException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private TimetableResponse toResponse(TimetableData timetableData,
                                         String role,
                                         String displayName,
                                         String programme) {
        List<TimetableEntryResponse> entries = timetableData.getSections().stream()
                .sorted(Comparator.comparing(Section::getStartTime))
                .map(this::toEntry)
                .toList();

        return new TimetableResponse(role, displayName, programme, entries.size(), entries);
    }

    private TimetableEntryResponse toEntry(Section section) {
        String day = section.getStartTime() == null ? "-" : toCliDayCode(section.getStartTime().getDayOfWeek());
        String start = section.getStartTime() == null ? "-" : section.getStartTime().format(TIME_FORMATTER);
        String end = section.getEndTime() == null ? "-" : section.getEndTime().format(TIME_FORMATTER);

        String title = section.getCourse() == null ? "-" : section.getCourse().getTitle();
        String code = section.getCourse() == null ? "-" : section.getCourse().getCourseCode();
        String type = section.getType() == null ? "-" : section.getType().name().toLowerCase(Locale.ROOT);

        List<String> instructorNames = section.getInstructors() == null
            ? List.of()
            : section.getInstructors().stream()
            .map(instructor -> instructor.getUserName() == null || instructor.getUserName().isBlank()
                ? instructor.getUserEID()
                : instructor.getUserName())
            .filter(name -> name != null && !name.isBlank())
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        String lecturers = instructorNames.isEmpty() ? "-" : String.join(", ", instructorNames);

        return new TimetableEntryResponse(
                section.getSectionId(),
                code,
                title,
                day,
                start,
                end,
                section.getVenue(),
                lecturers,
                type
        );
    }

    public record TimetableResponse(
            String role,
            String displayName,
            String programme,
            int totalCourses,
            List<TimetableEntryResponse> entries
    ) {
    }

    public record TimetableEntryResponse(
            Integer sectionId,
            String code,
            String title,
            String day,
            String start,
            String end,
            String venue,
            String lecturer,
            String type
    ) {
    }

    private static String toCliDayCode(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "M";
            case TUESDAY -> "T";
            case WEDNESDAY -> "W";
            case THURSDAY -> "R";
            case FRIDAY -> "F";
            case SATURDAY -> "S";
            default -> "-";
        };
    }
}
