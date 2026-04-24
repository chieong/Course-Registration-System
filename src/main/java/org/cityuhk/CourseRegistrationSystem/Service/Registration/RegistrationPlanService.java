package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPlanRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.PlanEntryRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistrationPlanService {

    private static final int MAX_PLAN_COUNT = 10;

    private final RegistrationPlanRepository registrationPlanRepository;
    private final PlanEntryRepositoryPort planEntryRepository;
    private final StudentRepositoryPort studentRepository;
    private final SectionRepositoryPort sectionRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final RegistrationService registrationService;

    public RegistrationPlanService(RegistrationPlanRepository registrationPlanRepository,
                                   PlanEntryRepositoryPort planEntryRepository,
                                   StudentRepositoryPort studentRepository,
                                   SectionRepositoryPort sectionRepository,
                                   RegistrationPeriodRepository registrationPeriodRepository,
                                   RegistrationRecordRepository registrationRecordRepository,
                                   RegistrationService registrationService) {
        this.registrationPlanRepository = registrationPlanRepository;
        this.planEntryRepository = planEntryRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.registrationService = registrationService;
    }

    @Transactional(readOnly = true)
    public List<RegistrationPlan> getPlanSet(Integer studentId) {
        List<RegistrationPlan> plans = registrationPlanRepository.findByStudentIdOrderByPriorityAsc(studentId);

        // The CLI prints plan entries after this service method returns, so initialize
        // the required lazy associations within the active transaction.
        for (RegistrationPlan plan : plans) {
            if (plan.getEntries() == null) {
                continue;
            }
            plan.getEntries().size();
            for (PlanEntry entry : plan.getEntries()) {
                Section section = entry.getSection();
                if (section == null) {
                    continue;
                }
                section.getSectionId();
                if (section.getCourse() != null) {
                    section.getCourse().getCourseCode();
                    if (section.getCourse().getSections() != null) {
                        section.getCourse().getSections().size();
                    }
                }
            }
        }

        return plans;
    }

     @Transactional
     public RegistrationPlan createPlan(Integer studentId, Integer requestedPriority) {
Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

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

        registrationPlanRepository.delete(plan);
    }

    @Transactional
    public PlanEntry addEntry(Integer planId, Integer sectionId, PlanEntry.EntryType entryType, boolean joinWaitlistOnAddFailure) {
        RegistrationPlan plan = registrationPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

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
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

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

    @Transactional
    public PlanSubmitResult saveOrSubmitPlan(Integer planId, LocalDateTime now) {
        RegistrationPlan plan = registrationPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Student student = plan.getStudent();
        if (student == null) {
            throw new RuntimeException("Student not found for plan");
        }

        RegistrationPeriod active = registrationPeriodRepository.findActivePeriod(student.getCohort(), now).orElse(null);
        if (active == null) {
            plan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
            plan.setApplySummary("Saved as draft (registration period inactive)");
            registrationPlanRepository.save(plan);
            return new PlanSubmitResult(false, "Draft saved. Registration period is not active.", plan);
        }

        try {
            applyPlanToRegistration(plan, now);
            plan.setApplyStatus(RegistrationPlan.ApplyStatus.APPLIED);
            plan.setApplyAttemptedAt(now);
            plan.setApplySummary("Submitted and applied successfully");
            registrationPlanRepository.save(plan);
            return new PlanSubmitResult(true, "Plan submitted and registration updated.", plan);
        } catch (RuntimeException ex) {
            plan.setApplyStatus(RegistrationPlan.ApplyStatus.FAILED);
            plan.setApplyAttemptedAt(now);
            plan.setApplySummary(ex.getMessage());
            registrationPlanRepository.save(plan);
            return new PlanSubmitResult(true, "Plan submission failed: " + ex.getMessage(), plan);
        }
    }

    private void applyPlanToRegistration(RegistrationPlan plan, LocalDateTime now) {
        Integer studentId = plan.getStudent().getStudentId();
        List<RegistrationRecord> currentRecords = registrationRecordRepository.findByStudentId(studentId);

        Set<Integer> desiredSectionIds = plan.getEntries().stream()
                .filter(entry -> entry.getEntryType() == PlanEntry.EntryType.SELECTED)
                .map(entry -> entry.getSection() == null ? null : entry.getSection().getSectionId())
                .filter(sectionId -> sectionId != null)
                .collect(Collectors.toSet());

        Set<Integer> currentSectionIds = currentRecords.stream()
                .map(record -> record.getSection().getSectionId())
                .collect(Collectors.toSet());

        Set<Integer> toDrop = new HashSet<>(currentSectionIds);
        toDrop.removeAll(desiredSectionIds);

        Set<Integer> toAdd = new HashSet<>(desiredSectionIds);
        toAdd.removeAll(currentSectionIds);

        for (Integer sectionId : toDrop) {
            registrationService.dropSection(studentId, sectionId, now);
        }

        for (PlanEntry entry : plan.getEntries()) {
            if (entry.getEntryType() != PlanEntry.EntryType.SELECTED) {
                continue;
            }

            if (entry.getSection() == null || entry.getSection().getSectionId() == null) {
                entry.setStatus(PlanEntry.EntryStatus.FAILED);
                entry.setFailureReason("Section not found");
                throw new RuntimeException("Plan contains invalid section");
            }

            Integer sectionId = entry.getSection().getSectionId();
            if (!toAdd.contains(sectionId)) {
                entry.setStatus(PlanEntry.EntryStatus.APPLIED);
                entry.setFailureReason(null);
                continue;
            }

            try {
                registrationService.addSection(studentId, sectionId, now);
                entry.setStatus(PlanEntry.EntryStatus.APPLIED);
                entry.setFailureReason(null);
            } catch (RuntimeException ex) {
                entry.setStatus(PlanEntry.EntryStatus.FAILED);
                entry.setFailureReason(ex.getMessage());
                throw new RuntimeException("Failed on section " + sectionId + ": " + ex.getMessage());
            }
        }
    }

    public record PlanSubmitResult(boolean submitted, String message, RegistrationPlan plan) {
    }
}
