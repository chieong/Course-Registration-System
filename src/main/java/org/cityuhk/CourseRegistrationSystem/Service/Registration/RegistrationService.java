package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Model.WaitlistRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Observer.SectionVacancyObserver;
import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {

    private final StudentRepositoryPort studentRepository;
    private final SectionRepositoryPort sectionRepository;
    private final RegistrationRecordRepositoryPort registrationRecordRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final WaitlistRecordRepository waitlistRecordRepository;
    private final List<SectionVacancyObserver> observers = new ArrayList<>();

    @Autowired
    public RegistrationService(
            StudentRepositoryPort studentRepository,
            SectionRepositoryPort sectionRepository,
            RegistrationRecordRepositoryPort registrationRecordRepository,
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
        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
        Section section =
                sectionRepository
                        .findById(sectionId)
                        .orElseThrow(() -> new RuntimeException("Section not found"));

        List<Integer> eligibleCohorts =
                registrationPeriodRepository.getActiveCohortByTime(LocalDateTime.now());
        if (!eligibleCohorts.contains(student.getCohort())) {
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
        Student student = studentRepository
                        .findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
        Section  section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        List<Integer> eligibleCohorts =
                registrationPeriodRepository.getActiveCohortByTime(LocalDateTime.now());
        if (!eligibleCohorts.contains(student.getCohort())) {
            throw new RuntimeException("Student not eligible to register");
        }


        Optional<RegistrationRecord> existingRecord =
                registrationRecordRepository.findByStudentIdAndSectionId(studentId, sectionId);
        if (existingRecord.isEmpty()) {
            throw new RuntimeException("Not enrolled");
        }
        RegistrationRecord registrationRecord = existingRecord.get();
        registrationRecordRepository.delete(registrationRecord);

        for (SectionVacancyObserver observer : observers) {
            observer.onVacancyOccurred(sectionId);
        }
    }

    public void dropWaitlist(Integer studentId, Integer sectionId) {
        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() -> new RuntimeException("Student not found"));



        List<Integer> eligibleCohorts =
                registrationPeriodRepository.getActiveCohortByTime(LocalDateTime.now());
        if (!eligibleCohorts.contains(student.getCohort())) {
            throw new RuntimeException("Student not eligible to register");
        }

        Optional<WaitlistRecord> existingRecord = waitlistRecordRepository.findByStudentIdAndSectionId(studentId, sectionId);
        if (existingRecord.isEmpty()) {
            throw new RuntimeException("Not waitlisted");
        }
        WaitlistRecord waitlistRecord = existingRecord.get();
        waitlistRecordRepository.delete(waitlistRecord);

        observers.removeIf(observer -> observer.getStudentId().equals(studentId));
    }

    public void addObserver(SectionVacancyObserver observer) {
        this.observers.add(observer);
    }

}
