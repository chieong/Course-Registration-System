package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.*;

@Entity
public class PlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer entryId;

    @ManyToOne(optional = false)
    private RegistrationPlan plan;

    @ManyToOne(optional = false)
    private Section section;

    private String entryType;
    private String status;
    private boolean joinWaitlistOnAddFailure;

    public PlanEntry() {}

    public PlanEntry(RegistrationPlan plan, Section section, String entryType) {
        this.plan = plan;
        this.section = section;
        this.entryType = entryType;
    }

    public Integer getEntryId() { return entryId; }
    public void setEntryId(Integer entryId) { this.entryId = entryId; }

    public RegistrationPlan getPlan() { return plan; }
    public void setPlan(RegistrationPlan plan) { this.plan = plan; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isJoinWaitlistOnAddFailure() { return joinWaitlistOnAddFailure; }
    public void setJoinWaitlistOnAddFailure(boolean joinWaitlistOnAddFailure) {
        this.joinWaitlistOnAddFailure = joinWaitlistOnAddFailure;
    }
}
