package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistrationPeriodValidator {

    private final RegistrationPeriodRepository registrationPeriodRepository;

    public RegistrationPeriodValidator(RegistrationPeriodRepository registrationPeriodRepository) {
        this.registrationPeriodRepository = registrationPeriodRepository;
    }

    public void validate(AdminPeriodRequest request) {
        if (request.getCohort() == null) {
            throw new RegistrationPeriodValidationException("Cohort is required");
        }
        if (request.getTerm() == null || request.getTerm().isBlank()) {
            throw new RegistrationPeriodValidationException("Term is required");
        }
        if (request.getStartDate() == null) {
            throw new RegistrationPeriodValidationException("Start date is required");
        }
        if (request.getEndDate() == null) {
            throw new RegistrationPeriodValidationException("End date is required");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new RegistrationPeriodValidationException("Start date must be before end date");
        }

        List<RegistrationPeriod> overlapping = registrationPeriodRepository.findOverlappingPeriods(
                request.getCohort(), request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new RegistrationPeriodOverlapException(
                    "Period overlaps with an existing period for cohort " + request.getCohort());
        }
    }
}
