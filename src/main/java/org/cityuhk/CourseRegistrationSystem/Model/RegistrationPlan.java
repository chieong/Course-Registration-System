package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class RegistrationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    @ManyToOne(optional = false)
    private Student student;

    private String term;
    private int priority;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanEntry> entries = new ArrayList<>();

    private String applyStatus;
    private String applySummary;

    public RegistrationPlan() {}

    public RegistrationPlan(Student student, String term, int priority) {
        this.student = student;
        this.term = term;
        this.priority = priority;
    }

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public List<PlanEntry> getEntries() { return entries; }

    public String getApplyStatus() { return applyStatus; }
    public void setApplyStatus(String applyStatus) { this.applyStatus = applyStatus; }

    public String getApplySummary() { return applySummary; }
    public void setApplySummary(String applySummary) { this.applySummary = applySummary; }

    public void addEntry(PlanEntry entry) {
        entry.setPlan(this);
        entries.add(entry);
    }

    public void removeEntry(PlanEntry entry) {
        entries.remove(entry);
        entry.setPlan(null);
    }
}
