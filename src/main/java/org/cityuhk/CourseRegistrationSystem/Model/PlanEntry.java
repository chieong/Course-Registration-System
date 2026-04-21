package org.cityuhk.CourseRegistrationSystem.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"plan_plan_id", "section_section_id"})
        }
)
public class PlanEntry {

    public enum EntryType {
        SELECTED,
        WAITLIST
    }

    public enum EntryStatus {
        PENDING,
        APPLIED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer entryId;

    @ManyToOne(optional = false)
    @JsonIgnore
    private RegistrationPlan plan;

    @ManyToOne(optional = false)
    private Section section;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    @Enumerated(EnumType.STRING)
    private EntryStatus status = EntryStatus.PENDING;

    private String failureReason;

    private boolean joinWaitlistOnAddFailure;

    public PlanEntry() {}

    public PlanEntry(RegistrationPlan plan, Section section, EntryType entryType) {
        this.plan = plan;
        this.section = section;
        this.entryType = entryType;
    }

    public Integer getEntryId() {
        return entryId;
    }

    public void setEntryId(Integer entryId) {
        this.entryId = entryId;
    }

    public RegistrationPlan getPlan() {
        return plan;
    }

    public void setPlan(RegistrationPlan plan) {
        this.plan = plan;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public EntryStatus getStatus() {
        return status;
    }

    public void setStatus(EntryStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isJoinWaitlistOnAddFailure() {
        return joinWaitlistOnAddFailure;
    }

    public void setJoinWaitlistOnAddFailure(boolean joinWaitlistOnAddFailure) {
        this.joinWaitlistOnAddFailure = joinWaitlistOnAddFailure;
    }
}
