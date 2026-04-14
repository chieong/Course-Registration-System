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
import java.util.Collections;
import java.util.List;

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

    @Transactional
    public void addSection(Integer studentId, Integer sectionId, LocalDateTime timestamp, Semester semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        
        if (registrationRecordRepository.exists(studentId, sectionId)) {
            throw new RuntimeException("Already enrolled");
        }
        
        int enrolled = registrationRecordRepository.countEnrolled(sectionId);
        registrationRecordRepository.save(student.addSection(section, timestamp, enrolled, semester));
    }

    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

    public Path ExportTimeTable(Integer studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<RegistrationRecord> records = registrationRecordRepository.findByStudentId(studentId);
        
        Collections.sort(records);

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
                    String row = record.toTimetableRow(dayFormatter, timeFormatter);
                    if (row != null) {
                        writer.write(row);
                        writer.newLine();
                    }
                }
            }
            return outputPath;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to export timetable", ex);
        }
    }
}