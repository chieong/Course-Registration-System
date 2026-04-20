package org.cityuhk.CourseRegistrationSystem.RestController.dto;

public class PlanEntryRequest {
    private Integer sectionId;
    private String entryType;
    private Boolean joinWaitlistOnAddFailure;

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public Boolean getJoinWaitlistOnAddFailure() {
        return joinWaitlistOnAddFailure;
    }

    public void setJoinWaitlistOnAddFailure(Boolean joinWaitlistOnAddFailure) {
        this.joinWaitlistOnAddFailure = joinWaitlistOnAddFailure;
    }
}
