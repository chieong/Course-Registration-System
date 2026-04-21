package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Observer.SectionVacancyObserver;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final WaitlistRecordRepository waitlistRecordRepository;
    private final List<SectionVacancyObserver> observers = new ArrayList<>();

    @Autowired
    public RegistrationService(
            StudentRepository studentRepository,
            SectionRepository sectionRepository,
            RegistrationRecordRepository registrationRecordRepository,
            RegistrationPeriodRepository registrationPeriodRepository,
            WaitlistRecordRepository waitlistRecordRepository) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
        this.waitlistRecordRepository = waitlistRecordRepository;
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

    public void waitListSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Section  section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        List<Integer> eligibleCohorts = registrationPeriodRepository.getActiveCohortByTime(LocalDateTime.now());
        if(!eligibleCohorts.contains(student.getCohort())){
            throw new RuntimeException("Student not eligible to register");
        }

        if (waitlistRecordRepository.exists(studentId,sectionId)) {
            throw new RuntimeException("Already waitlisted");
        }

        int waitlisted = waitlistRecordRepository.countWaitlisted(sectionId);
        waitlistRecordRepository.save(student.waitlistSection(section,timestamp, waitlisted));
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

        for (SectionVacancyObserver observer : observers) {
            observer.onVacancyOccurred(sectionId);
        }
    }

    public void addObserver(SectionVacancyObserver observer) {
        this.observers.add(observer);
    }

}
