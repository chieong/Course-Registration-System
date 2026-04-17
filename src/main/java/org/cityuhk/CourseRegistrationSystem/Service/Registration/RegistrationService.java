package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
    public void addSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        
        if (registrationRecordRepository.exists(studentId, sectionId)) {
            throw new RuntimeException("Already enrolled");
        }
        
        int enrolled = registrationRecordRepository.countEnrolled(sectionId);
        registrationRecordRepository.save(student.addSection(section, timestamp, enrolled));
    }

    @Transactional
    public void dropSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Optional<Student> existingStudent = studentRepository.findById(studentId);
        if (!existingStudent.isPresent()) {
            throw new RuntimeException("Student not found");
        }
        Optional<Section> existingSection = sectionRepository.findById(sectionId);
        if (!existingSection.isPresent()) {
            throw new RuntimeException("Section not found");
        }
        
        Optional<RegistrationRecord> existingRecord = registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId);
        if (!existingRecord.isPresent()) {
            throw new RuntimeException("Not enrolled");
        }
        RegistrationRecord registrationRecord = existingRecord.get();
        registrationRecordRepository.delete(registrationRecord);
    }
}
