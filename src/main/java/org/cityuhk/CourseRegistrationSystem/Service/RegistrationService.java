package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Service class for managing Student entities.

@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final RegistrationRecordRepository registrationRecordRepository;

    @Autowired
    public RegistrationService(
            StudentRepository studentRepository,
            SectionRepository sectionRepository,
            RegistrationRecordRepository registrationRecordRepository) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.registrationRecordRepository = registrationRecordRepository;
    }

    // public Student saveStudent(Student student) {
    //     return studentRepository.save(student);
    // }
    //
    // public List<Student> getAllStudents() {
    //     return studentRepository.findAll();
    // }
    //
    // public Optional<Student> getStudentById(Integer id) {
    //     return studentRepository.findById(id);
    // }

    @Transactional
    public void addSection(Integer studentId, Integer sectionId, LocalDateTime timestamp, Semester semester) {
        Optional<Student> existingStudent = studentRepository.findById(studentId);
        if (!existingStudent.isPresent()) {
            throw new RuntimeException("Student not found");
        }
        Optional<Section> existingSection = sectionRepository.findById(sectionId);
        if (!existingSection.isPresent()) {
            throw new RuntimeException("Section not found");
        }
        if (registrationRecordRepository.exists(studentId, sectionId)) {
            throw new RuntimeException("Already enrolled");
        }
        Student student = existingStudent.get();
        Section section = existingSection.get();
        int enrolled = registrationRecordRepository.countEnrolled(sectionId);
        registrationRecordRepository.save(student.addSection(section, timestamp, enrolled, semester));
    }

    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

    public Path ExportTimeTable(Integer studentId) {
        Optional<Student> existingStudent = studentRepository.findById(studentId);
        if (!existingStudent.isPresent()) {
            throw new RuntimeException("Student not found");
        }

        List<RegistrationRecord> records = registrationRecordRepository.findByStudentId(studentId);
        records.sort(Comparator
                .comparing((RegistrationRecord r) -> {
                    Section s = r.getSection();
                    return s != null ? s.getStartTime() : null;
                }, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(r -> {
                    Section s = r.getSection();
                    return s != null ? s.getEndTime() : null;
                }, Comparator.nullsLast(Comparator.naturalOrder())));

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            Path outputPath = Files.createTempFile("student-" + studentId + "-timetable-", ".txt");
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                writer.write("STUDENT TIMETABLE");
                writer.newLine();
                writer.write("Student ID: " + studentId);
                writer.newLine();
                writer.write("Generated At: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                writer.newLine();
                writer.newLine();

                String header = String.format("%-6s %-13s %-12s %-8s %-18s %-22s", "DAY", "TIME", "COURSE", "SEC", "TYPE", "VENUE");
                writer.write(header);
                writer.newLine();
                writer.write("----------------------------------------------------------------------");
                writer.newLine();

                for (RegistrationRecord record : records) {
                    Section section = record.getSection();
                    if (section == null) {
                        continue;
                    }

                    String courseCode = section.getCourse() != null ? section.getCourse().getCourseCode() : "";
                    String sectionType = section.getType() != null ? section.getType().name() : "";
                    String venue = section.getVenue() != null ? section.getVenue() : "";

                    String day = section.getStartTime() != null ? section.getStartTime().format(dayFormatter) : "N/A";
                    String timeRange = (section.getStartTime() != null && section.getEndTime() != null)
                            ? section.getStartTime().format(timeFormatter) + "-" + section.getEndTime().format(timeFormatter)
                            : "N/A";

                    String row = String.format(
                            "%-6s %-13s %-12s %-8s %-18s %-22s",
                            day,
                            timeRange,
                            trimToWidth(courseCode, 12),
                            section.getSectionID(),
                            trimToWidth(sectionType, 18),
                            trimToWidth(venue, 22));
                    writer.write(row);
                    writer.newLine();
                }
            }
            return outputPath;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to export timetable", ex);
        }
    }

    private String trimToWidth(String value, int width) {
        if (value == null) {
            return "";
        }
        if (value.length() <= width) {
            return value;
        }
        return value.substring(0, Math.max(0, width - 3)) + "...";
    }
}
