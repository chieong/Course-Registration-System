package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPlanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlanCleanupService {

    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final RegistrationPlanRepository registrationPlanRepository;

    public PlanCleanupService(RegistrationPeriodRepository registrationPeriodRepository,
                              RegistrationPlanRepository registrationPlanRepository) {
        this.registrationPeriodRepository = registrationPeriodRepository;
        this.registrationPlanRepository = registrationPlanRepository;
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupExpiredTerms() {
        LocalDateTime now = LocalDateTime.now();
        List<String> terms = registrationPeriodRepository.findDistinctTerms();

        for (String term : terms) {
            List<RegistrationPeriod> periods = registrationPeriodRepository.findByTermOrderByEndDateTimeDesc(term);
            if (periods.isEmpty()) {
                continue;
            }

            RegistrationPeriod lastPeriod = periods.get(0);
            if (lastPeriod.getEndDateTime() != null && now.isAfter(lastPeriod.getEndDateTime())) {
                registrationPlanRepository.deleteByTerm(term);
            }
        }
    }
}
