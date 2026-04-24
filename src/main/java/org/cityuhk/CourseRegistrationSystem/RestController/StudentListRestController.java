package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/studentlist")
@PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
public class StudentListRestController {

    private final InstructorRepository instructorRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final AdminRepository adminRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public StudentListRestController(InstructorRepository instructorRepository,
                                     RegistrationRecordRepository registrationRecordRepository,
                                     AdminRepository adminRepository) {
        this.instructorRepository = instructorRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.adminRepository = adminRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStudentList(Authentication authentication,
                                            @RequestParam(required = false, name = "instructor") String instructorEid) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        try {
            String currentUserEid = authentication.getName();
            boolean isAdmin = adminRepository.findByUserEID(currentUserEid).isPresent();

            if (!isAdmin && instructorEid != null && !instructorEid.isBlank() && !currentUserEid.equalsIgnoreCase(instructorEid.trim())) {
                return ResponseEntity.status(403).body("Instructors can only view their own student list");
            }

            List<Instructor> targetInstructors = resolveTargetInstructors(isAdmin, currentUserEid, instructorEid);
            List<SectionStudentListResponse> response = new ArrayList<>();

            for (Instructor instructor : targetInstructors) {
                Set<Section> sections = instructor.getSections() == null ? Set.of() : instructor.getSections();

                List<Section> sortedSections = sections.stream()
                        .sorted(Comparator.comparing(Section::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();

                for (Section section : sortedSections) {
                    List<StudentRowResponse> students = registrationRecordRepository.findBySectionId(section.getSectionId()).stream()
                            .sorted(Comparator.comparing(record -> record.getStudent().getStudentId(), Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(this::toStudentRow)
                            .toList();

                    response.add(toSectionResponse(instructor, section, students));
                }
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private List<Instructor> resolveTargetInstructors(boolean isAdmin,
                                                      String currentUserEid,
                                                      String requestedInstructorEid) {
        if (isAdmin) {
            if (requestedInstructorEid != null && !requestedInstructorEid.isBlank()) {
                Optional<Instructor> instructor = instructorRepository.findByUserEIDWithSections(requestedInstructorEid.trim());
                if (instructor.isEmpty()) {
                    throw new RuntimeException("Instructor not found");
                }
                return List.of(instructor.get());
            }
            return instructorRepository.findAll();
        }

        Optional<Instructor> instructor = instructorRepository.findByUserEIDWithSections(currentUserEid);
        if (instructor.isEmpty()) {
            throw new RuntimeException("Instructor not found");
        }
        return List.of(instructor.get());
    }

    private StudentRowResponse toStudentRow(RegistrationRecord record) {
        Student student = record.getStudent();

        String studentCode = student.getUserEID() == null || student.getUserEID().isBlank()
                ? String.valueOf(student.getStudentId())
                : student.getUserEID();

        int year = estimateYear(student.getCohort());

        String email = (student.getUserEID() == null || student.getUserEID().isBlank())
                ? "-"
                : student.getUserEID().toLowerCase(Locale.ROOT) + "@cityu.edu.hk";

        return new StudentRowResponse(
                studentCode,
                student.getUserName(),
                student.getMajor(),
                "Year " + year,
                email,
                "Active"
        );
    }

    private SectionStudentListResponse toSectionResponse(Instructor instructor,
                                                         Section section,
                                                         List<StudentRowResponse> students) {
        LocalDateTime start = section.getStartTime();
        LocalDateTime end = section.getEndTime();

        String day = start == null
                ? "TBA"
                : start.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        String time = (start == null || end == null)
                ? "TBA"
                : start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);

        String instructorName = instructor.getUserName() == null || instructor.getUserName().isBlank()
                ? instructor.getUserEID()
                : instructor.getUserName();

        return new SectionStudentListResponse(
                section.getSectionId(),
                section.getCourse() == null ? "-" : section.getCourse().getCourseCode(),
                section.getCourse() == null ? "-" : section.getCourse().getTitle(),
                instructorName,
                day,
                time,
                students
        );
    }

    private int estimateYear(Integer cohort) {
        if (cohort == null || cohort <= 0) {
            return 1;
        }
        int currentYear = LocalDate.now().getYear();
        int calculated = currentYear - cohort + 1;
        return Math.max(1, calculated);
    }

    public record SectionStudentListResponse(
            Integer sectionId,
            String code,
            String title,
            String teacher,
            String day,
            String time,
            List<StudentRowResponse> students
    ) {
    }

    public record StudentRowResponse(
            String id,
            String name,
            String programme,
            String year,
            String email,
            String status
    ) {
    }
}
