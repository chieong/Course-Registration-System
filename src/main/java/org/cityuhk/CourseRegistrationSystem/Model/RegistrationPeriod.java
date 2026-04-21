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
    private String term;

    public RegistrationPeriod() {}

    public RegistrationPeriod(int cohort, LocalDateTime startDateTime, LocalDateTime endDateTime, String term) {
        this.cohort = cohort;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.term = term;
    }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    public int getCohort() { return cohort; }
    public void setCohort(int cohort) { this.cohort = cohort; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
}

