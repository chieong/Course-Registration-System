package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationPlanRestControllerTest {

    @Mock
    private RegistrationPlanService registrationPlanService;

    @InjectMocks
    private RegistrationPlanRestController controller;

    @Test
    void getPlanSet_returnsDtoPayload() {
        Course course = new Course();
        course.setCourseCode("CSC318");
        course.setTitle("Software Engineering");
        course.setCredits(3);

        Section section = new Section();
        section.setSectionId(7);
        section.setCourse(course);
        section.setVenue("Room B204");
        section.setType(Section.Type.LECTURE);
        section.setTime(LocalDateTime.of(2026, 4, 22, 14, 0), LocalDateTime.of(2026, 4, 22, 16, 0));

        RegistrationPlan plan = new RegistrationPlan();
        plan.setPlanId(1);
        plan.setPriority(1);
        plan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);

        PlanEntry entry = new PlanEntry();
        entry.setEntryId(9);
        entry.setEntryType(PlanEntry.EntryType.SELECTED);
        entry.setStatus(PlanEntry.EntryStatus.PENDING);
        entry.setSection(section);
        entry.setPlan(plan);
        plan.addEntry(entry);

        when(registrationPlanService.getPlanSet(1001)).thenReturn(List.of(plan));

        ResponseEntity<?> response = controller.getPlanSet(1001);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());

        @SuppressWarnings("unchecked")
        List<RegistrationPlanRestController.PlanResponse> body = (List<RegistrationPlanRestController.PlanResponse>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(Integer.valueOf(1), body.get(0).planId());
        assertEquals(1, body.get(0).entries().size());
        assertEquals("CSC318", body.get(0).entries().get(0).courseCode());
    }

    @Test
    void saveOrSubmit_returnsPayloadWithMessage() {
        RegistrationPlan plan = new RegistrationPlan();
        plan.setPlanId(5);
        plan.setPriority(1);
        plan.setApplyStatus(RegistrationPlan.ApplyStatus.APPLIED);
        plan.setApplySummary("Submitted and applied successfully");

        RegistrationPlanService.PlanSubmitResult result =
                new RegistrationPlanService.PlanSubmitResult(true, "Plan submitted and registration updated.", plan);

        when(registrationPlanService.saveOrSubmitPlan(org.mockito.ArgumentMatchers.eq(5), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(result);

        ResponseEntity<?> response = controller.saveOrSubmit(5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(RegistrationPlanRestController.PlanSaveResponse.class, response.getBody());

        RegistrationPlanRestController.PlanSaveResponse body =
                (RegistrationPlanRestController.PlanSaveResponse) response.getBody();
        assertEquals(true, body.submitted());
        assertEquals("Plan submitted and registration updated.", body.message());
        assertEquals(Integer.valueOf(5), body.plan().planId());
    }
}
