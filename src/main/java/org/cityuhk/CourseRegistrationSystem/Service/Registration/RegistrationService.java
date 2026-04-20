package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;

    @Autowired
    public RegistrationService(
            StudentRepository studentRepository,
            SectionRepository sectionRepository,
            RegistrationRecordRepository registrationRecordRepository,
            RegistrationPeriodRepository registrationPeriodRepository) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
    }

    @Transactional
    public void addSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        List<Integer> eligibleCohorts = registrationPeriodRepository.getActiveCohortByTime(LocalDateTime.now());
        if(!eligibleCohorts.contains(student.getCohort())){
            throw new RuntimeException("Student not eligible to register");
        }


        if (registrationRecordRepository.exists(studentId, sectionId)) {
            throw new RuntimeException("Already enrolled");
        }
        
        int enrolled = registrationRecordRepository.countEnrolled(sectionId);
        registrationRecordRepository.save(student.addSection(section, timestamp, enrolled));
    }

    @Transactional
    public void dropSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        // TODO FIX DUPLICATE TO MAKE INTELLIJ IDEA SHUT UP

        List<Integer> eligibleCohorts = registrationPeriodRepository.getActiveCohortByTime(LocalDateTime.now());
        if(!eligibleCohorts.contains(student.getCohort())){
            throw new RuntimeException("Student not eligible to register");
        }

        Optional<RegistrationRecord> existingRecord = registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId);
        if (existingRecord.isEmpty()) {
            throw new RuntimeException("Not enrolled");
        }
        RegistrationRecord registrationRecord = existingRecord.get();
        registrationRecordRepository.delete(registrationRecord);
    }
}
