package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminPeriodRequest {
    private Integer periodId;
    private Integer cohort;
    private String term;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Integer getPeriodId() {
        return periodId;
    }
    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }
    public Integer getCohort() {
        return cohort;
    }
    public void setCohort(Integer cohort) {
        this.cohort = cohort;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
