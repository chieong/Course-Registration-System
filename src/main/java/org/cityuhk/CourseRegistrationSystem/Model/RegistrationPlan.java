package org.cityuhk.CourseRegistrationSystem.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_student_id", "term", "priority"})
        }
)
public class RegistrationPlan {

    public enum ApplyStatus {
        NOT_ATTEMPTED,
        APPLIED,
        FAILED,
        SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Student student;

    private String term;

    private Integer priority;

    private LocalDateTime applyAttemptedAt;

    @Enumerated(EnumType.STRING)
    private ApplyStatus applyStatus = ApplyStatus.NOT_ATTEMPTED;

    private String applySummary;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanEntry> entries = new ArrayList<>();

    public Integer getPlanId() {
        return planId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getApplyAttemptedAt() {
        return applyAttemptedAt;
    }

    public void setApplyAttemptedAt(LocalDateTime applyAttemptedAt) {
        this.applyAttemptedAt = applyAttemptedAt;
    }

    public ApplyStatus getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(ApplyStatus applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getApplySummary() {
        return applySummary;
    }

    public void setApplySummary(String applySummary) {
        this.applySummary = applySummary;
    }

    public List<PlanEntry> getEntries() {
        return entries;
    }

    public void addEntry(PlanEntry entry) {
        entry.setPlan(this);
        this.entries.add(entry);
    }

    public void removeEntry(PlanEntry entry) {
        this.entries.remove(entry);
        entry.setPlan(null);
    }
}
