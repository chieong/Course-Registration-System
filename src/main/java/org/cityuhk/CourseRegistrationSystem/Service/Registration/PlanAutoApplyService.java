package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPlanRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlanAutoApplyService {

    private final RegistrationPeriodRepository registrationPeriodRepository;
    private final RegistrationPlanRepository registrationPlanRepository;
    private final RegistrationRecordRepository registrationRecordRepository;
    private final RegistrationService registrationService;

    public PlanAutoApplyService(RegistrationPeriodRepository registrationPeriodRepository,
                                RegistrationPlanRepository registrationPlanRepository,
                                RegistrationRecordRepository registrationRecordRepository,
                                RegistrationService registrationService) {
        this.registrationPeriodRepository = registrationPeriodRepository;
        this.registrationPlanRepository = registrationPlanRepository;
        this.registrationRecordRepository = registrationRecordRepository;
        this.registrationService = registrationService;
    }

    @Scheduled(fixedDelay = 60000)
    public void autoApplyActivePeriodPlans() {
        LocalDateTime now = LocalDateTime.now();
        List<RegistrationPeriod> activePeriods = registrationPeriodRepository.findActivePeriods(now);

        for (RegistrationPeriod period : activePeriods) {
            processPeriod(period, now);
        }
    }

    @Transactional
    protected void processPeriod(RegistrationPeriod period, LocalDateTime now) {
        List<RegistrationPlan> plans = registrationPlanRepository
                .findByTermAndCohortOrderByStudentAndPriority(period.getTerm(), period.getCohort());

        Map<Integer, List<RegistrationPlan>> plansByStudent = new HashMap<>();
        for (RegistrationPlan plan : plans) {
            plansByStudent.computeIfAbsent(plan.getStudent().getStudentId(), ignored -> new ArrayList<>()).add(plan);
        }

        for (Map.Entry<Integer, List<RegistrationPlan>> studentPlans : plansByStudent.entrySet()) {
            List<RegistrationPlan> candidates = studentPlans.getValue().stream()
                    .sorted(Comparator.comparing(RegistrationPlan::getPriority))
                    .collect(Collectors.toList());

            boolean applied = false;
            for (RegistrationPlan candidate : candidates) {
                if (candidate.getApplyStatus() == RegistrationPlan.ApplyStatus.APPLIED
                        || candidate.getApplyStatus() == RegistrationPlan.ApplyStatus.SKIPPED) {
                    continue;
                }

                boolean success = tryApplyCandidate(candidate, now);
                if (success) {
                    applied = true;
                    break;
                }
            }

            if (applied) {
                for (RegistrationPlan candidate : candidates) {
                    if (candidate.getApplyStatus() == RegistrationPlan.ApplyStatus.NOT_ATTEMPTED) {
                        candidate.setApplyStatus(RegistrationPlan.ApplyStatus.SKIPPED);
                        candidate.setApplySummary("Skipped after higher-priority plan applied");
                        registrationPlanRepository.save(candidate);
                    }
                }
            }
        }
    }

    private boolean tryApplyCandidate(RegistrationPlan candidate, LocalDateTime now) {
        try {
            applyCandidate(candidate, now);
            candidate.setApplyStatus(RegistrationPlan.ApplyStatus.APPLIED);
            candidate.setApplyAttemptedAt(now);
            candidate.setApplySummary("Applied successfully");
            registrationPlanRepository.save(candidate);
            return true;
        } catch (RuntimeException ex) {
            candidate.setApplyStatus(RegistrationPlan.ApplyStatus.FAILED);
            candidate.setApplyAttemptedAt(now);
            candidate.setApplySummary(ex.getMessage());
            registrationPlanRepository.save(candidate);
            return false;
        }
    }

    private void applyCandidate(RegistrationPlan candidate, LocalDateTime now) {
        Integer studentId = candidate.getStudent().getStudentId();
        List<RegistrationRecord> currentRecords = registrationRecordRepository.findByStudentId(studentId);

        Set<Integer> desiredSectionIds = candidate.getEntries().stream()
                .filter(entry -> entry.getEntryType() == PlanEntry.EntryType.SELECTED)
                .map(entry -> entry.getSection().getSectionId())
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

        for (PlanEntry entry : candidate.getEntries()) {
            if (entry.getEntryType() != PlanEntry.EntryType.SELECTED) {
                continue;
            }

            Integer sectionId = entry.getSection().getSectionId();
            if (!toAdd.contains(sectionId)) {
                entry.setStatus(PlanEntry.EntryStatus.APPLIED);
                continue;
            }

            try {
                registrationService.addSection(studentId, sectionId, now);
                entry.setStatus(PlanEntry.EntryStatus.APPLIED);
                entry.setFailureReason(null);
            } catch (RuntimeException ex) {
                entry.setStatus(PlanEntry.EntryStatus.FAILED);
                entry.setFailureReason(ex.getMessage());

                if (entry.isJoinWaitlistOnAddFailure()) {
                    entry.setFailureReason(ex.getMessage() + " (waitlist requested; waitlist automation pending)");
                }

                throw new RuntimeException("Plan " + candidate.getPlanId() + " failed on section " + sectionId + ": " + ex.getMessage());
            }
        }
    }
}
