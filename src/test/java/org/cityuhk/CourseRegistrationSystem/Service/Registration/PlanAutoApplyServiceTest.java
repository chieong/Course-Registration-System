package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPlanRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanAutoApplyServiceTest {

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @Mock
    private RegistrationPlanRepository registrationPlanRepository;

    @Mock
    private RegistrationRecordRepository registrationRecordRepository;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private PlanAutoApplyService planAutoApplyService;

    private Student student;
    private RegistrationPeriod period;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        student = new Student.StudentBuilder()
                .withStudentId(1)
                .withUserEID("s001")
                .withName("Alice")
                .withPassword("pw")
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(120)
                .build();

        period = new RegistrationPeriod();
        period.setCohort(2024);

        now = LocalDateTime.of(2026, 4, 21, 10, 0);
    }

    @Test
    void processPeriod_WhenHigherPriorityPlanApplies_LowerPriorityPlanIsSkipped() {
        RegistrationPlan first = buildPlan(101, 1, buildSelectedEntry(501, false));
        RegistrationPlan second = buildPlan(102, 2, buildSelectedEntry(502, false));

        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of(first, second));
        when(registrationRecordRepository.findByStudentId(1)).thenReturn(List.of());
        doNothing().when(registrationService).addSection(1, 501, now);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        planAutoApplyService.processPeriod(period, now);

        assertEquals(RegistrationPlan.ApplyStatus.APPLIED, first.getApplyStatus());
        assertEquals("Applied successfully", first.getApplySummary());
        assertEquals(RegistrationPlan.ApplyStatus.SKIPPED, second.getApplyStatus());
        assertEquals("Skipped after higher-priority plan applied", second.getApplySummary());
    }

    @Test
    void processPeriod_WhenFirstPlanFails_SecondPlanStillGetsApplied() {
        PlanEntry firstEntry = buildSelectedEntry(601, false);
        PlanEntry secondEntry = buildSelectedEntry(602, false);

        RegistrationPlan first = buildPlan(201, 1, firstEntry);
        RegistrationPlan second = buildPlan(202, 2, secondEntry);

        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of(first, second));
        when(registrationRecordRepository.findByStudentId(1)).thenReturn(List.of());
        doThrow(new RuntimeException("No seat available")).when(registrationService).addSection(1, 601, now);
        doNothing().when(registrationService).addSection(1, 602, now);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        planAutoApplyService.processPeriod(period, now);

        assertEquals(RegistrationPlan.ApplyStatus.FAILED, first.getApplyStatus());
        assertTrue(first.getApplySummary().contains("No seat available"));
        assertEquals(PlanEntry.EntryStatus.FAILED, firstEntry.getStatus());

        assertEquals(RegistrationPlan.ApplyStatus.APPLIED, second.getApplyStatus());
        assertEquals(PlanEntry.EntryStatus.APPLIED, secondEntry.getStatus());
    }

    @Test
    void processPeriod_WhenAddFailsAndWaitlistRequested_StoresWaitlistAwareFailureReason() {
        PlanEntry entry = buildSelectedEntry(701, true);
        RegistrationPlan onlyPlan = buildPlan(301, 1, entry);

        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of(onlyPlan));
        when(registrationRecordRepository.findByStudentId(1)).thenReturn(List.of());
        doThrow(new RuntimeException("Capacity full")).when(registrationService).addSection(1, 701, now);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        planAutoApplyService.processPeriod(period, now);

        assertEquals(RegistrationPlan.ApplyStatus.FAILED, onlyPlan.getApplyStatus());
        assertEquals(PlanEntry.EntryStatus.FAILED, entry.getStatus());
        assertTrue(entry.getFailureReason().contains("waitlist requested; waitlist automation pending"));
        assertTrue(onlyPlan.getApplySummary().contains("Capacity full"));
    }

    @Test
    void autoApplyActivePeriodPlans_WhenNoActivePeriod_DoesNothing() {
        when(registrationPeriodRepository.findActivePeriods(any(LocalDateTime.class))).thenReturn(List.of());

        planAutoApplyService.autoApplyActivePeriodPlans();

        verify(registrationPlanRepository, never()).findByCohortOrderByStudentAndPriority(any(Integer.class));
    }

    @Test
    void autoApplyActivePeriodPlans_WhenMultiplePeriods_ProcessesEachCohort() {
        RegistrationPeriod p1 = new RegistrationPeriod();
        p1.setCohort(2024);
        RegistrationPeriod p2 = new RegistrationPeriod();
        p2.setCohort(2025);

        when(registrationPeriodRepository.findActivePeriods(any(LocalDateTime.class))).thenReturn(List.of(p1, p2));
        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of());
        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2025)).thenReturn(List.of());

        planAutoApplyService.autoApplyActivePeriodPlans();

        verify(registrationPlanRepository).findByCohortOrderByStudentAndPriority(2024);
        verify(registrationPlanRepository).findByCohortOrderByStudentAndPriority(2025);
    }

    @Test
    void processPeriod_WhenCandidateAlreadyAppliedOrSkipped_DoesNotReapply() {
        RegistrationPlan applied = buildPlan(401, 1, buildSelectedEntry(801, false));
        applied.setApplyStatus(RegistrationPlan.ApplyStatus.APPLIED);

        RegistrationPlan skipped = buildPlan(402, 2, buildSelectedEntry(802, false));
        skipped.setApplyStatus(RegistrationPlan.ApplyStatus.SKIPPED);

        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of(applied, skipped));

        planAutoApplyService.processPeriod(period, now);

        verify(registrationService, never()).addSection(any(Integer.class), any(Integer.class), any(LocalDateTime.class));
        verify(registrationPlanRepository, never()).save(any(RegistrationPlan.class));
    }

    @Test
    void processPeriod_ComputesDropAndAddDeltaAndMarksAlreadyPresentSectionsApplied() {
        PlanEntry alreadyPresent = buildSelectedEntry(900, false);
        PlanEntry toAdd = buildSelectedEntry(901, false);
        PlanEntry nonSelected = buildSelectedEntry(902, false);
        nonSelected.setEntryType(PlanEntry.EntryType.WAITLIST);

        RegistrationPlan candidate = buildPlan(501, 1, alreadyPresent, toAdd, nonSelected);

        RegistrationRecord keepRecord = buildRecord(900);
        RegistrationRecord dropRecord = buildRecord(999);

        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of(candidate));
        when(registrationRecordRepository.findByStudentId(1)).thenReturn(List.of(keepRecord, dropRecord));
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        planAutoApplyService.processPeriod(period, now);

        verify(registrationService).dropSection(1, 999, now);
        verify(registrationService).addSection(1, 901, now);
        verify(registrationService, never()).addSection(1, 900, now);

        assertEquals(PlanEntry.EntryStatus.APPLIED, alreadyPresent.getStatus());
        assertEquals(PlanEntry.EntryStatus.APPLIED, toAdd.getStatus());
        assertEquals(PlanEntry.EntryStatus.PENDING, nonSelected.getStatus());
    }

    @Test
    void processPeriod_WhenAppliedPlanExists_OnlyNotAttemptedPlansBecomeSkipped() {
        RegistrationPlan applied = buildPlan(601, 1, buildSelectedEntry(910, false));
        RegistrationPlan failed = buildPlan(602, 2, buildSelectedEntry(911, false));
        RegistrationPlan notAttempted = buildPlan(603, 3, buildSelectedEntry(912, false));

        applied.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);
        failed.setApplyStatus(RegistrationPlan.ApplyStatus.FAILED);
        notAttempted.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);

        when(registrationPlanRepository.findByCohortOrderByStudentAndPriority(2024)).thenReturn(List.of(applied, failed, notAttempted));
        when(registrationRecordRepository.findByStudentId(1)).thenReturn(List.of());
        doNothing().when(registrationService).addSection(1, 910, now);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        planAutoApplyService.processPeriod(period, now);

        assertEquals(RegistrationPlan.ApplyStatus.APPLIED, applied.getApplyStatus());
        assertEquals(RegistrationPlan.ApplyStatus.FAILED, failed.getApplyStatus());
        assertEquals(RegistrationPlan.ApplyStatus.SKIPPED, notAttempted.getApplyStatus());
        verify(registrationPlanRepository, times(2)).save(any(RegistrationPlan.class));
    }

    private RegistrationPlan buildPlan(int planId, int priority, PlanEntry... entries) {
        RegistrationPlan plan = new RegistrationPlan(student, priority);
        plan.setPlanId(planId);
        plan.setApplyStatus(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED);

        for (PlanEntry entry : entries) {
            entry.setPlan(plan);
            plan.addEntry(entry);
        }
        return plan;
    }

    private PlanEntry buildSelectedEntry(int sectionId, boolean joinWaitlistOnFailure) {
        Course course = new Course("CS" + sectionId, "Course " + sectionId, 3, "", Set.of(), Set.of(), Set.of());
        Section section = new Section();
        section.setSectionId(sectionId);
        section.setCourse(course);

        PlanEntry entry = new PlanEntry();
        entry.setSection(section);
        entry.setEntryType(PlanEntry.EntryType.SELECTED);
        entry.setStatus(PlanEntry.EntryStatus.PENDING);
        entry.setJoinWaitlistOnAddFailure(joinWaitlistOnFailure);
        return entry;
    }

    private RegistrationRecord buildRecord(int sectionId) {
        Course course = new Course("CS" + sectionId, "Course " + sectionId, 3, "", Set.of(), Set.of(), Set.of());
        Section section = new Section();
        section.setSectionId(sectionId);
        section.setCourse(course);
        return new RegistrationRecord(student, section, now.minusMinutes(5));
    }
}
