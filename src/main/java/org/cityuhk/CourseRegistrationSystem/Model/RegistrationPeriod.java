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

    private int cohort;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public RegistrationPeriod (Integer cohort, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.cohort = cohort;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        if(endDateTime.isBefore(startDateTime)) {
            throw new RuntimeException("Start date cannot be before end date");
        }
    }

    public int getCohort() {
        return cohort;
    }

    public Integer getPeriodId() {
        return periodId;
    }
}

