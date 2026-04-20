package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class RegistrationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int periodId;

    private final int cohort;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    public RegistrationPeriod (Integer cohort, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.cohort = cohort;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        if(startDateTime.isBefore(endDateTime)) {
            throw new RuntimeException("Start date cannot be before end date");
        }
    }

    public boolean isCurrentlyActive() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return startDateTime.isAfter(currentDateTime) && endDateTime.isBefore(currentDateTime);
    }

    public int getCohort() {
        return cohort;
    }

    public Integer getPeriodId() {
        return periodId;
    }
}

