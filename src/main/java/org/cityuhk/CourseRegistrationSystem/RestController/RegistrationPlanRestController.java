package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
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

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@PreAuthorize("hasRole('STUDENT')")
public class RegistrationPlanRestController {

    private final RegistrationPlanService registrationPlanService;

    public RegistrationPlanRestController(RegistrationPlanService registrationPlanService) {
        this.registrationPlanService = registrationPlanService;
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getPlanSet(@PathVariable Integer studentId) {
        try {
            List<RegistrationPlan> plans = registrationPlanService.getPlanSet(studentId);
            return ResponseEntity.ok(plans);
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
            return ResponseEntity.ok(created);
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
            return ResponseEntity.ok(created);
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

    @PutMapping("/{studentId}/reorder")
    public ResponseEntity<?> reorder(@PathVariable Integer studentId,
                                     @RequestBody PlanReorderRequest request) {
        try {
            if (request.getOrderedPlanIds() == null || request.getOrderedPlanIds().isEmpty()) {
                throw new RuntimeException("orderedPlanIds is required");
            }
            List<RegistrationPlan> reordered = registrationPlanService.reorderPlans(studentId, request.getOrderedPlanIds());
            return ResponseEntity.ok(reordered);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
