package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.PlanCreateRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.PlanEntryRequest;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.PlanReorderRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/plans")
@PreAuthorize("hasRole('STUDENT')")
public class RegistrationPlanRestController {

    private final RegistrationPlanService registrationPlanService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RegistrationPlanRestController(RegistrationPlanService registrationPlanService) {
        this.registrationPlanService = registrationPlanService;
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getPlanSet(@PathVariable Integer studentId) {
        try {
            List<RegistrationPlan> plans = registrationPlanService.getPlanSet(studentId);
            return ResponseEntity.ok(plans.stream().map(this::toPlanResponse).toList());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{studentId}")
    public ResponseEntity<?> createPlan(@PathVariable Integer studentId,
                                        @RequestBody(required = false) PlanCreateRequest request) {
        try {
            Integer requestedPriority = request != null ? request.getPriority() : null;
            RegistrationPlan created = registrationPlanService.createPlan(studentId, requestedPriority);
            return ResponseEntity.ok(toPlanResponse(created));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<?> removePlan(@PathVariable Integer planId) {
        try {
            registrationPlanService.removePlan(planId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{planId}/entries")
    public ResponseEntity<?> addEntry(@PathVariable Integer planId, @RequestBody PlanEntryRequest request) {
        try {
            if (request.getSectionId() == null) {
                throw new RuntimeException("Section ID is required");
            }
            if (request.getEntryType() == null || request.getEntryType().isBlank()) {
                throw new RuntimeException("Entry type is required");
            }

            PlanEntry.EntryType entryType = PlanEntry.EntryType.valueOf(request.getEntryType().trim().toUpperCase());
            boolean joinWaitlistOnAddFailure = Boolean.TRUE.equals(request.getJoinWaitlistOnAddFailure());
            PlanEntry created = registrationPlanService.addEntry(planId, request.getSectionId(), entryType, joinWaitlistOnAddFailure);
            return ResponseEntity.ok(toEntryResponse(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid entry type. Use SELECTED or WAITLIST");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{planId}/entries/{entryId}")
    public ResponseEntity<?> removeEntry(@PathVariable Integer planId, @PathVariable Integer entryId) {
        try {
            registrationPlanService.removeEntry(planId, entryId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{planId}/save")
    public ResponseEntity<?> saveOrSubmit(@PathVariable Integer planId) {
        try {
            RegistrationPlanService.PlanSubmitResult result = registrationPlanService.saveOrSubmitPlan(planId, LocalDateTime.now());
            PlanResponse planResponse = toPlanResponse(result.plan());
            return ResponseEntity.ok(new PlanSaveResponse(result.submitted(), result.message(), planResponse));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{studentId}/reorder")
    public ResponseEntity<?> reorder(@PathVariable Integer studentId,
                                     @RequestBody PlanReorderRequest request) {
        try {
            if (request.getOrderedPlanIds() == null || request.getOrderedPlanIds().isEmpty()) {
                throw new RuntimeException("orderedPlanIds is required");
            }
            List<RegistrationPlan> reordered = registrationPlanService.reorderPlans(studentId, request.getOrderedPlanIds());
                return ResponseEntity.ok(reordered.stream().map(this::toPlanResponse).toList());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

            private PlanResponse toPlanResponse(RegistrationPlan plan) {
            List<PlanEntryResponse> entries = plan.getEntries() == null
                ? List.of()
                : plan.getEntries().stream().map(this::toEntryResponse).toList();

            String attemptedAt = plan.getApplyAttemptedAt() == null
                ? null
                : plan.getApplyAttemptedAt().format(DATE_TIME_FORMATTER);

            return new PlanResponse(
                plan.getPlanId(),
                plan.getPriority(),
                plan.getApplyStatus() == null ? null : plan.getApplyStatus().name(),
                attemptedAt,
                plan.getApplySummary(),
                entries
            );
            }

            private PlanEntryResponse toEntryResponse(PlanEntry entry) {
            Section section = entry.getSection();
            LocalDateTime start = section == null ? null : section.getStartTime();
            LocalDateTime end = section == null ? null : section.getEndTime();

            String day = start == null
                ? "-"
                : start.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            String startTime = start == null ? "-" : start.format(DateTimeFormatter.ofPattern("HH:mm"));
            String endTime = end == null ? "-" : end.format(DateTimeFormatter.ofPattern("HH:mm"));

            return new PlanEntryResponse(
                entry.getEntryId(),
                entry.getEntryType() == null ? null : entry.getEntryType().name(),
                entry.getStatus() == null ? null : entry.getStatus().name(),
                entry.isJoinWaitlistOnAddFailure(),
                entry.getFailureReason(),
                section == null ? null : section.getSectionId(),
                section != null && section.getCourse() != null ? section.getCourse().getCourseCode() : "-",
                section != null && section.getCourse() != null ? section.getCourse().getTitle() : "-",
                section != null && section.getCourse() != null ? section.getCourse().getCredits() : 0,
                day,
                startTime,
                endTime,
                section == null || section.getVenue() == null ? "-" : section.getVenue(),
                section == null || section.getType() == null ? "-" : section.getType().name()
            );
            }

            public record PlanResponse(
                Integer planId,
                Integer priority,
                String applyStatus,
                String applyAttemptedAt,
                String applySummary,
                List<PlanEntryResponse> entries
            ) {
            }

            public record PlanEntryResponse(
                Integer entryId,
                String entryType,
                String status,
                boolean joinWaitlistOnAddFailure,
                String failureReason,
                Integer sectionId,
                String courseCode,
                String title,
                int credits,
                String day,
                String startTime,
                String endTime,
                String venue,
                String sectionType
            ) {
            }

            public record PlanSaveResponse(
                boolean submitted,
                String message,
                PlanResponse plan
            ) {
            }
}
