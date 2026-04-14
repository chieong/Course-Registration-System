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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
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

        try {
            Path outputPath = Files.createTempFile("student-" + studentId + "-timetable-", ".csv");
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                writer.write("studentId,sectionId,courseCode,courseTitle,sectionType,venue,startTime,endTime,registeredAt");
                writer.newLine();

                for (RegistrationRecord record : records) {
                    Section section = record.getSection();
                    String courseCode = section.getCourse() != null ? section.getCourse().getCourseCode() : "";
                    String courseTitle = section.getCourse() != null ? section.getCourse().getTitle() : "";
                    String sectionType = section.getType() != null ? section.getType().name() : "";
                    String venue = section.getVenue() != null ? section.getVenue() : "";
                    String startTime = section.getStartTime() != null ? section.getStartTime().toString() : "";
                    String endTime = section.getEndTime() != null ? section.getEndTime().toString() : "";
                    String registeredAt = record.getTimestamp() != null ? record.getTimestamp().toString() : "";

                    writer.write(studentId + ","
                            + section.getSectionID() + ","
                            + csvEscape(courseCode) + ","
                            + csvEscape(courseTitle) + ","
                            + csvEscape(sectionType) + ","
                            + csvEscape(venue) + ","
                            + csvEscape(startTime) + ","
                            + csvEscape(endTime) + ","
                            + csvEscape(registeredAt));
                    writer.newLine();
                }
            }
            return outputPath;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to export timetable", ex);
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
