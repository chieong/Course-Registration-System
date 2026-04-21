package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminPeriodRequest {
    private Integer periodId;
    private int cohort;
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
    public void setCohort(int cohort) {
        this.cohort = cohort;
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
