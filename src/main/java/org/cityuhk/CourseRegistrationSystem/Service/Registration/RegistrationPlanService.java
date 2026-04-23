package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPlanRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.PlanEntryRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegistrationPlanService {

    private static final int MAX_PLAN_COUNT = 10;

    private final RegistrationPlanRepository registrationPlanRepository;
    private final PlanEntryRepositoryPort planEntryRepository;
    private final StudentRepositoryPort studentRepository;
    private final SectionRepositoryPort sectionRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;

    public RegistrationPlanService(RegistrationPlanRepository registrationPlanRepository,
                                   PlanEntryRepositoryPort planEntryRepository,
                                   StudentRepositoryPort studentRepository,
                                   SectionRepositoryPort sectionRepository,
                                   RegistrationPeriodRepository registrationPeriodRepository) {
        this.registrationPlanRepository = registrationPlanRepository;
        this.planEntryRepository = planEntryRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
    }

    @Transactional(readOnly = true)
    public List<RegistrationPlan> getPlanSet(Integer studentId) {
        return registrationPlanRepository.findByStudentIdOrderByPriorityAsc(studentId);
    }

     @Transactional
     public RegistrationPlan createPlan(Integer studentId, Integer requestedPriority) {
Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ensurePlanEditable(student,LocalDateTime.now());

        long currentCount = registrationPlanRepository.countByStudentIdForPlanLimit(studentId);
        if (currentCount >= MAX_PLAN_COUNT) {
            throw new RuntimeException("Maximum 10 plans allowed");
        }

        int priority = requestedPriority != null ? requestedPriority : (int) currentCount + 1;
        if (priority < 1 || priority > MAX_PLAN_COUNT) {
            throw new RuntimeException("Priority must be between 1 and 10");
        }

        if (registrationPlanRepository.existsByStudentIdAndPriority(studentId, priority)) {
            throw new RuntimeException("Priority slot already in use");
        }

        RegistrationPlan plan = new RegistrationPlan();
        plan.setStudent(student);
        plan.setPriority(priority);
        plan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
        plan.setApplySummary("Awaiting period start");

        return registrationPlanRepository.save(plan);
    }

    @Transactional
    public void removePlan(Integer planId) {
        RegistrationPlan plan = registrationPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        ensurePlanEditable(plan.getStudent(), LocalDateTime.now());
        registrationPlanRepository.delete(plan);
    }

    @Transactional
    public PlanEntry addEntry(Integer planId, Integer sectionId, PlanEntry.EntryType entryType, boolean joinWaitlistOnAddFailure) {
        RegistrationPlan plan = registrationPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        ensurePlanEditable(plan.getStudent(), LocalDateTime.now());

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        boolean duplicate = plan.getEntries().stream()
            .anyMatch(e -> e.getSection() != null
                && java.util.Objects.equals(e.getSection().getSectionId(), sectionId));
        if (duplicate) {
            throw new RuntimeException("Section already exists in plan");
        }

        PlanEntry entry = new PlanEntry();
        entry.setPlan(plan);
        entry.setSection(section);
        entry.setEntryType(entryType);
        entry.setStatus(PlanEntry.EntryStatus.PENDING);
        entry.setJoinWaitlistOnAddFailure(joinWaitlistOnAddFailure);
        plan.addEntry(entry);

        registrationPlanRepository.save(plan);
        return entry;
    }

    @Transactional
    public void removeEntry(Integer planId, Integer entryId) {
        RegistrationPlan plan = registrationPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        ensurePlanEditable(plan.getStudent(), LocalDateTime.now());

        PlanEntry entry = planEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Plan entry not found"));

        if (!entry.getPlan().getPlanId().equals(plan.getPlanId())) {
            throw new RuntimeException("Plan entry does not belong to plan");
        }

        plan.removeEntry(entry);
        registrationPlanRepository.save(plan);
    }

    @Transactional
    public List<RegistrationPlan> reorderPlans(Integer studentId, List<Integer> orderedPlanIds) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ensurePlanEditable(student,LocalDateTime.now());

        List<RegistrationPlan> existing = registrationPlanRepository.findByStudentIdOrderByPriorityAsc(studentId);
        if (existing.size() != orderedPlanIds.size()) {
            throw new RuntimeException("Reorder list size mismatch");
        }

        List<Integer> missing = new ArrayList<>();
        for (RegistrationPlan plan : existing) {
            if (!orderedPlanIds.contains(plan.getPlanId())) {
                missing.add(plan.getPlanId());
            }
        }
        if (!missing.isEmpty()) {
            throw new RuntimeException("Reorder list missing plans: " + missing);
        }

        // Two-phase priority update avoids temporary unique-constraint collisions.
        for (int i = 0; i < orderedPlanIds.size(); i++) {
            Integer planId = orderedPlanIds.get(i);
            RegistrationPlan plan = existing.stream()
                    .filter(p -> p.getPlanId().equals(planId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid plan ID in reorder list: " + planId));
            plan.setPriority(100 + i + 1);
        }
        registrationPlanRepository.saveAll(existing);

        for (int i = 0; i < orderedPlanIds.size(); i++) {
            Integer planId = orderedPlanIds.get(i);
            RegistrationPlan plan = existing.stream()
                    .filter(p -> p.getPlanId().equals(planId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid plan ID in reorder list: " + planId));
            plan.setPriority(i + 1);
        }

        return registrationPlanRepository.saveAll(existing);
    }

    private void ensurePlanEditable(Student student, LocalDateTime now) {
        RegistrationPeriod active = registrationPeriodRepository.findActivePeriod(student.getCohort(), now).orElse(null);
        if (active != null) {
            throw new RuntimeException("Plans are read-only during active registration period");
        }

        List<RegistrationPeriod> configuredPeriods = registrationPeriodRepository.findByCohortOrderByStartDateTime(student.getCohort());
        if (!configuredPeriods.isEmpty() && !now.isBefore(configuredPeriods.get(0).getStartDateTime())) {
            throw new RuntimeException("Plans cannot be edited after period start");
        }
    }
}
