package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import org.cityuhk.CourseRegistrationSystem.Persistence.LocalDateTimeStringConverter;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"cohort", "startDateTime", "endDateTime"})
        })
public class RegistrationPeriod {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Integer periodId;

     private int cohort;

     public RegistrationPeriod() {}

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private LocalDateTime startDateTime;

    @Convert(converter = LocalDateTimeStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private LocalDateTime endDateTime;

     public RegistrationPeriod(
             Integer cohort, LocalDateTime startDateTime, LocalDateTime endDateTime) {
         this.cohort = cohort;
         this.startDateTime = startDateTime;
         this.endDateTime = endDateTime;
         if (endDateTime.isBefore(startDateTime)) {
             throw new RuntimeException("Start date cannot be before end date");
         }
     }

     public Integer getPeriodId() {
         return periodId;
     }

public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }

    public int getCohort() {
        return cohort;
    }

    public void setCohort(int cohort) {
        this.cohort = cohort;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }
}
