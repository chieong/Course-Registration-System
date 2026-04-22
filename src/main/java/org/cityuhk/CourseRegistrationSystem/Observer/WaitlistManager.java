package org.cityuhk.CourseRegistrationSystem.Observer;


import org.cityuhk.CourseRegistrationSystem.Repository.WaitlistRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class WaitlistManager implements SectionVacancyObserver {
    private final WaitlistRecordRepository waitlistRepository;
    private final RegistrationService registrationService;
    private final Integer studentId;

    @Autowired
    public WaitlistManager(WaitlistRecordRepository waitlistRepository, RegistrationService registrationService, Integer studentId) {
        this.waitlistRepository = waitlistRepository;
        this.registrationService = registrationService;
        this.studentId = studentId;
        this.registrationService.addObserver(this);
    }

    @Override
    public void onVacancyOccurred(Integer sectionId) {
        waitlistRepository.findFirstBySectionIdOrderByTimestampAsc(sectionId)
                .ifPresent(record -> {
                    try {
                        registrationService.addSection(
                                record.getStudent().getStudentId(),
                                sectionId,
                                LocalDateTime.now()
                        );
                        waitlistRepository.delete(record);
                        System.out.println("Auto-enrolled student " + record.getStudent().getStudentId());

                    } catch (RuntimeException e) {
                        System.err.println("Failed to auto-enroll from waitlist: " + e.getMessage());
                    }
                });
    }

    @Override
    public Integer getStudentId() {
        return studentId;
    }
}
