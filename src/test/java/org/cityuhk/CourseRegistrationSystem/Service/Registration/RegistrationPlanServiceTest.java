package org.cityuhk.CourseRegistrationSystem.Service.Registration;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.PlanEntry;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPlanRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.PlanEntryRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationPlanServiceTest {

    @Mock
    private RegistrationPlanRepository registrationPlanRepository;

    @Mock
    private PlanEntryRepositoryPort planEntryRepository;

    @Mock
    private StudentRepositoryPort studentRepository;

    @Mock
    private SectionRepositoryPort sectionRepository;

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @Mock
    private RegistrationRecordRepository registrationRecordRepository;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationPlanService registrationPlanService;

    private Student student;

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
    }

    @Test
    void createPlan_WhenPriorityIsNull_AssignsNextPriorityAndDefaultStatus() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(2L);
        when(registrationPlanRepository.existsByStudentIdAndPriority(1, 3)).thenReturn(false);
        when(registrationPlanRepository.save(any(RegistrationPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPlan created = registrationPlanService.createPlan(1, null);

        assertEquals(3, created.getPriority());
        assertEquals(RegistrationPlan.ApplyStatus.NOT_ATTEMPTED, created.getApplyStatus());
        assertEquals("Awaiting period start", created.getApplySummary());
        assertSame(student, created.getStudent());
    }

    @Test
    void getPlanSet_ReturnsPlansFromRepository() {
        RegistrationPlan p1 = new RegistrationPlan(student, 1);
        RegistrationPlan p2 = new RegistrationPlan(student, 2);
        when(registrationPlanRepository.findByStudentIdOrderByPriorityAsc(1))
                .thenReturn(List.of(p1, p2));

        List<RegistrationPlan> found = registrationPlanService.getPlanSet(1);

        assertEquals(2, found.size());
        assertSame(p1, found.get(0));
        assertSame(p2, found.get(1));
    }

    @Test
    void createPlan_WhenStudentNotFound_ThrowsError() {
        when(studentRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.createPlan(999, 1));

        assertEquals("Student not found", ex.getMessage());
    }

    @Test
    void createPlan_WhenMaxPlanCountReached_RejectsCreation() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.createPlan(1, null));

        assertEquals("Maximum 10 plans allowed", ex.getMessage());
    }

    @Test
    void createPlan_WhenPriorityOutOfRange_RejectsCreation() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(0L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.createPlan(1, 0));

        assertEquals("Priority must be between 1 and 10", ex.getMessage());
    }

    @Test
    void createPlan_WhenPriorityAboveRange_RejectsCreation() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(0L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.createPlan(1, 11));

        assertEquals("Priority must be between 1 and 10", ex.getMessage());
    }

    @Test
    void createPlan_WhenPriorityAlreadyUsed_RejectsCreation() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(1L);
        when(registrationPlanRepository.existsByStudentIdAndPriority(1, 2)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.createPlan(1, 2));

        assertEquals("Priority slot already in use", ex.getMessage());
    }

    @Test
    void createPlan_WhenFirstPeriodAlreadyStarted_StillAllowsEditing() {
        RegistrationPeriod configured = new RegistrationPeriod();
        configured.setStartDateTime(LocalDateTime.now().minusMinutes(1));

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(0L);
        when(registrationPlanRepository.existsByStudentIdAndPriority(1, 1)).thenReturn(false);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPlan created = registrationPlanService.createPlan(1, 1);

        assertEquals(1, created.getPriority());
    }

    @Test
    void createPlan_WhenConfiguredPeriodInFuture_AllowsEditing() {
        RegistrationPeriod configured = new RegistrationPeriod();
        configured.setStartDateTime(LocalDateTime.now().plusDays(1));

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of(configured));
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(0L);
        when(registrationPlanRepository.existsByStudentIdAndPriority(1, 1)).thenReturn(false);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPlan created = registrationPlanService.createPlan(1, 1);

        assertEquals(1, created.getPriority());
    }

    @Test
    void createPlan_WhenActiveRegistrationPeriodExists_StillAllowsEditing() {
        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPlanRepository.countByStudentIdForPlanLimit(1)).thenReturn(0L);
        when(registrationPlanRepository.existsByStudentIdAndPriority(1, 1)).thenReturn(false);
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPlan created = registrationPlanService.createPlan(1, 1);

        assertEquals(1, created.getPriority());
    }

    @Test
    void addEntry_WhenPlanNotFound_ThrowsError() {
        when(registrationPlanRepository.findById(10)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.addEntry(10, 20, PlanEntry.EntryType.SELECTED, false));

        assertEquals("Plan not found", ex.getMessage());
    }

    @Test
    void addEntry_WhenSectionNotFound_ThrowsError() {
        RegistrationPlan plan = new RegistrationPlan(student, 1);
        plan.setPlanId(10);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(plan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(sectionRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.addEntry(10, 999, PlanEntry.EntryType.SELECTED, false));

        assertEquals("Section not found", ex.getMessage());
    }

    @Test
    void addEntry_WhenDuplicateSectionExists_RejectsEntryByValueEquality() {
        RegistrationPlan plan = new RegistrationPlan(student, 1);
        plan.setPlanId(10);

        Course course = new Course("CS200", "Course", 3, "", Set.of(), Set.of(), Set.of());
        Section existingSection = new Section();
        existingSection.setSectionId(200);
        existingSection.setCourse(course);

        PlanEntry existingEntry = new PlanEntry();
        existingEntry.setSection(existingSection);
        existingEntry.setEntryType(PlanEntry.EntryType.SELECTED);
        plan.addEntry(existingEntry);

        Section incomingSection = new Section();
        incomingSection.setSectionId(Integer.valueOf(200));
        incomingSection.setCourse(course);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(plan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(sectionRepository.findById(200)).thenReturn(Optional.of(incomingSection));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.addEntry(10, 200, PlanEntry.EntryType.SELECTED, false));

        assertEquals("Section already exists in plan", ex.getMessage());
    }

    @Test
    void addEntry_WhenExistingEntryHasNullSection_AllowsNonDuplicateInsert() {
        RegistrationPlan plan = new RegistrationPlan(student, 1);
        plan.setPlanId(10);

        PlanEntry existingEntry = new PlanEntry();
        existingEntry.setSection(null);
        existingEntry.setEntryType(PlanEntry.EntryType.SELECTED);
        plan.addEntry(existingEntry);

        Course course = new Course("CS220", "Course", 3, "", Set.of(), Set.of(), Set.of());
        Section incomingSection = new Section();
        incomingSection.setSectionId(220);
        incomingSection.setCourse(course);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(plan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(sectionRepository.findById(220)).thenReturn(Optional.of(incomingSection));
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanEntry created = registrationPlanService.addEntry(10, 220, PlanEntry.EntryType.SELECTED, false);

        assertEquals(PlanEntry.EntryStatus.PENDING, created.getStatus());
        assertEquals(2, plan.getEntries().size());
    }

    @Test
    void addEntry_WhenExistingEntryHasDifferentSection_AllowsInsert() {
        RegistrationPlan plan = new RegistrationPlan(student, 1);
        plan.setPlanId(10);

        Course course = new Course("CS230", "Course", 3, "", Set.of(), Set.of(), Set.of());
        Section existingSection = new Section();
        existingSection.setSectionId(230);
        existingSection.setCourse(course);
        PlanEntry existingEntry = new PlanEntry();
        existingEntry.setSection(existingSection);
        existingEntry.setEntryType(PlanEntry.EntryType.SELECTED);
        plan.addEntry(existingEntry);

        Section incomingSection = new Section();
        incomingSection.setSectionId(231);
        incomingSection.setCourse(course);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(plan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(sectionRepository.findById(231)).thenReturn(Optional.of(incomingSection));
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanEntry created = registrationPlanService.addEntry(10, 231, PlanEntry.EntryType.SELECTED, false);

        assertEquals(PlanEntry.EntryStatus.PENDING, created.getStatus());
        assertEquals(2, plan.getEntries().size());
    }

    @Test
    void addEntry_WhenValid_AddsPendingEntryAndPersistsPlan() {
        RegistrationPlan plan = new RegistrationPlan(student, 1);
        plan.setPlanId(10);

        Course course = new Course("CS201", "Algo", 3, "", Set.of(), Set.of(), Set.of());
        Section section = new Section();
        section.setSectionId(20);
        section.setCourse(course);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(plan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(sectionRepository.findById(20)).thenReturn(Optional.of(section));
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanEntry entry = registrationPlanService.addEntry(10, 20, PlanEntry.EntryType.SELECTED, true);

        assertEquals(PlanEntry.EntryStatus.PENDING, entry.getStatus());
        assertEquals(PlanEntry.EntryType.SELECTED, entry.getEntryType());
        assertTrue(entry.isJoinWaitlistOnAddFailure());
        assertSame(plan, entry.getPlan());
    }

    @Test
    void removePlan_WhenPlanNotFound_ThrowsError() {
        when(registrationPlanRepository.findById(123)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.removePlan(123));

        assertEquals("Plan not found", ex.getMessage());
    }

    @Test
    void removePlan_WhenPlanExistsAndEditable_DeletesPlan() {
        RegistrationPlan plan = new RegistrationPlan(student, 1);
        plan.setPlanId(10);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(plan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());

        registrationPlanService.removePlan(10);

        verify(registrationPlanRepository).delete(plan);
    }

    @Test
    void reorderPlans_UsesProvidedOrderAndCompactsPriorities() {
        RegistrationPlan first = new RegistrationPlan(student, 1);
        first.setPlanId(11);
        RegistrationPlan second = new RegistrationPlan(student, 2);
        second.setPlanId(22);

        List<RegistrationPlan> existing = new ArrayList<>(List.of(first, second));

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.findByStudentIdOrderByPriorityAsc(1)).thenReturn(existing);
        when(registrationPlanRepository.saveAll(any(List.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<RegistrationPlan> reordered = registrationPlanService.reorderPlans(1, List.of(22, 11));

        assertEquals(2, reordered.size());
        assertEquals(2, first.getPriority());
        assertEquals(1, second.getPriority());
        verify(registrationPlanRepository, times(2)).saveAll(any(List.class));
    }

        @Test
        void reorderPlans_WhenStudentNotFound_ThrowsError() {
        when(studentRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> registrationPlanService.reorderPlans(1, List.of(1, 2)));

        assertEquals("Student not found", ex.getMessage());
        }

        @Test
        void reorderPlans_WhenListSizeMismatch_ThrowsError() {
        RegistrationPlan first = new RegistrationPlan(student, 1);
        first.setPlanId(11);
        RegistrationPlan second = new RegistrationPlan(student, 2);
        second.setPlanId(22);

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.findByStudentIdOrderByPriorityAsc(1))
            .thenReturn(List.of(first, second));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> registrationPlanService.reorderPlans(1, List.of(11)));

        assertEquals("Reorder list size mismatch", ex.getMessage());
        }

        @Test
        void reorderPlans_WhenListMissesPlans_ThrowsError() {
        RegistrationPlan first = new RegistrationPlan(student, 1);
        first.setPlanId(11);
        RegistrationPlan second = new RegistrationPlan(student, 2);
        second.setPlanId(22);

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.findByStudentIdOrderByPriorityAsc(1))
            .thenReturn(List.of(first, second));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> registrationPlanService.reorderPlans(1, List.of(11, 99)));

        assertTrue(ex.getMessage().contains("Reorder list missing plans"));
        assertTrue(ex.getMessage().contains("22"));
        }

    @Test
    void reorderPlans_WhenUnknownPlanIdBypassesMissingCheck_ThrowsInvalidIdInFirstPass() {
        RegistrationPlan first = new RegistrationPlan(student, 1);
        first.setPlanId(11);
        RegistrationPlan duplicate = new RegistrationPlan(student, 2);
        duplicate.setPlanId(11);

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.findByStudentIdOrderByPriorityAsc(1))
                .thenReturn(new ArrayList<>(List.of(first, duplicate)));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.reorderPlans(1, List.of(11, 99)));

        assertTrue(ex.getMessage().contains("Invalid plan ID in reorder list: 99"));
    }

    @Test
    void reorderPlans_WhenSaveAllMutatesExisting_ThrowsInvalidIdInSecondPass() {
        RegistrationPlan first = new RegistrationPlan(student, 1);
        first.setPlanId(11);
        RegistrationPlan second = new RegistrationPlan(student, 2);
        second.setPlanId(22);

        ArrayList<RegistrationPlan> existing = new ArrayList<>(List.of(first, second));
        AtomicInteger saveAllCalls = new AtomicInteger(0);

        when(studentRepository.findById(1)).thenReturn(Optional.of(student));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(registrationPlanRepository.findByStudentIdOrderByPriorityAsc(1)).thenReturn(existing);
        when(registrationPlanRepository.saveAll(any(List.class))).thenAnswer(invocation -> {
            List<RegistrationPlan> saved = invocation.getArgument(0);
            if (saveAllCalls.getAndIncrement() == 0) {
                saved.removeIf(p -> p.getPlanId().equals(22));
            }
            return saved;
        });

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.reorderPlans(1, List.of(11, 22)));

        assertTrue(ex.getMessage().contains("Invalid plan ID in reorder list: 22"));
    }

    @Test
    void removeEntry_WhenEntryBelongsToOtherPlan_ThrowsBusinessError() {
        RegistrationPlan ownerPlan = new RegistrationPlan(student, 1);
        ownerPlan.setPlanId(10);

        RegistrationPlan otherPlan = new RegistrationPlan(student, 2);
        otherPlan.setPlanId(99);

        PlanEntry entry = new PlanEntry();
        entry.setEntryId(300);
        entry.setPlan(otherPlan);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(ownerPlan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(planEntryRepository.findById(300)).thenReturn(Optional.of(entry));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.removeEntry(10, 300));

        assertEquals("Plan entry does not belong to plan", ex.getMessage());
    }

    @Test
    void removeEntry_WhenPlanNotFound_ThrowsError() {
        when(registrationPlanRepository.findById(10)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.removeEntry(10, 300));

        assertEquals("Plan not found", ex.getMessage());
    }

    @Test
    void removeEntry_WhenEntryNotFound_ThrowsError() {
        RegistrationPlan ownerPlan = new RegistrationPlan(student, 1);
        ownerPlan.setPlanId(10);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(ownerPlan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(planEntryRepository.findById(300)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> registrationPlanService.removeEntry(10, 300));

        assertEquals("Plan entry not found", ex.getMessage());
    }

    @Test
    void removeEntry_WhenValid_RemovesEntryAndSavesPlan() {
        RegistrationPlan ownerPlan = new RegistrationPlan(student, 1);
        ownerPlan.setPlanId(10);

        PlanEntry entry = new PlanEntry();
        entry.setEntryId(300);
        entry.setPlan(ownerPlan);
        ownerPlan.addEntry(entry);

        when(registrationPlanRepository.findById(10)).thenReturn(Optional.of(ownerPlan));
        when(registrationPeriodRepository.findActivePeriod(eq(2024), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(registrationPeriodRepository.findByCohortOrderByStartDateTime(2024)).thenReturn(List.of());
        when(planEntryRepository.findById(300)).thenReturn(Optional.of(entry));
        when(registrationPlanRepository.save(any(RegistrationPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrationPlanService.removeEntry(10, 300);

        assertTrue(ownerPlan.getEntries().isEmpty());
        verify(registrationPlanRepository).save(ownerPlan);
    }
}
