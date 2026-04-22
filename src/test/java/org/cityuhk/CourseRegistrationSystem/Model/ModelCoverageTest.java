package org.cityuhk.CourseRegistrationSystem.Model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelCoverageTest {

    private static Student buildStudent(int maxSemesterCredit) {
        return new Student.StudentBuilder()
                .withStudentId(1)
                .withUserEID("s001")
                .withName("Alice")
                .withPassword("pw")
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(maxSemesterCredit)
                .withMajor("CS")
                .withCohort(2024)
                .withDepartment("CS")
                .withMaxDegreeCredit(120)
                .build();
    }

    private static Course buildCourse(String code, int credits) {
        return new Course(code, "Title " + code, credits, "desc", "2026A",
                new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    private static Section buildSection(Course course, int enrollCapacity) {
        Section section = new Section();
        section.setSectionId(1);
        section.setCourse(course);
        section.setEnrollCapacity(enrollCapacity);
        section.setWaitlistCapacity(5);
        section.setVenue("Room A");
        section.setType(Section.Type.LECTURE);
        return section;
    }

    private static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void course_PrerequisiteAndExclusiveChecks_WorkAsExpected() {
        Course prerequisite = buildCourse("CS101", 3);
        Course exclusive = buildCourse("CS102", 3);
        Course course = buildCourse("CS201", 3);
        course.setPrerequisiteCourses(Set.of(prerequisite));
        course.setExclusiveCourses(Set.of(exclusive));

        assertTrue(course.satisfyPrerequisites(Set.of(prerequisite)));
        assertFalse(course.satisfyPrerequisites(Set.of()));
        assertTrue(course.notTakenExclusives(Set.of(prerequisite)));
        assertFalse(course.notTakenExclusives(Set.of(exclusive)));
    }

    @Test
    void course_HasCreditsAndAddCredits_DelegatesCorrectly() {
        Course course = buildCourse("CS301", 4);
        Student lowLimitStudent = buildStudent(3);
        Student highLimitStudent = buildStudent(10);

        assertTrue(course.hasCredits(lowLimitStudent));
        assertFalse(course.hasCredits(highLimitStudent));
        assertEquals(14, course.addCredits(10));
    }

    @Test
    void section_ConstructorAndSetTime_ValidateWithCurrentImplementation() {
        Course course = buildCourse("CS100", 3);
        LocalDateTime start = LocalDateTime.of(2026, 4, 21, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 21, 10, 0);

        assertDoesNotThrow(() -> new Section(course, 30, 5, start, end, "Room A"));

        Section section = new Section();
        assertDoesNotThrow(() -> section.setTime(start, end));
        assertThrows(IllegalArgumentException.class, () -> section.setTime(end, start));
    }

    @Test
    void section_OverlapAndAddCredits_WorkAsExpected() {
        Course course = buildCourse("CS360", 2);
        Section first = buildSection(course, 10);
        Section second = buildSection(course, 10);

        setPrivateField(first, "startTime", LocalDateTime.of(2026, 4, 21, 9, 0));
        setPrivateField(first, "endTime", LocalDateTime.of(2026, 4, 21, 10, 0));
        setPrivateField(second, "startTime", LocalDateTime.of(2026, 4, 21, 9, 30));
        setPrivateField(second, "endTime", LocalDateTime.of(2026, 4, 21, 10, 30));

        assertTrue(first.overlaps(second));
        setPrivateField(second, "startTime", LocalDateTime.of(2026, 4, 21, 11, 0));
        setPrivateField(second, "endTime", LocalDateTime.of(2026, 4, 21, 12, 0));
        assertFalse(first.overlaps(second));
        assertEquals(9, first.addCredits(7));
    }

    @Test
    void student_CreditValidationAndCourseChecks_WorkWithInternalState() {
        Student student = buildStudent(10);
        setPrivateField(student, "minSemesterCredit", 3);

        Section section = buildSection(buildCourse("CS401", 3), 20);
        RegistrationRecord record = new RegistrationRecord(student, section, LocalDateTime.now());
        setPrivateField(student, "registrationRecords", Set.of(record));

        assertTrue(student.validateSemesterCreditCount(0));
        assertFalse(student.validateSemesterCreditCount(20));

        Course prerequisite = buildCourse("CS101", 3);
        Course exclusive = buildCourse("CS102", 3);
        Course target = buildCourse("CS500", 3);
        target.setPrerequisiteCourses(Set.of(prerequisite));
        target.setExclusiveCourses(Set.of(exclusive));

        setPrivateField(student, "completedCourses", Set.of(prerequisite));
        assertTrue(student.satisfyPrerequisites(target));
        assertTrue(student.notTakenExclusives(target));

        setPrivateField(student, "completedCourses", Set.of(prerequisite, exclusive));
        assertFalse(student.notTakenExclusives(target));
    }

    @Test
    void registrationRecord_CompareToAndTimeAccessors_HandleNullsAndOrdering() {
        Student student = buildStudent(10);
        Section earlySection = buildSection(buildCourse("CS511", 3), 20);
        Section lateSection = buildSection(buildCourse("CS512", 3), 20);
        setPrivateField(earlySection, "startTime", LocalDateTime.of(2026, 4, 21, 9, 0));
        setPrivateField(earlySection, "endTime", LocalDateTime.of(2026, 4, 21, 10, 0));
        setPrivateField(lateSection, "startTime", LocalDateTime.of(2026, 4, 21, 11, 0));
        setPrivateField(lateSection, "endTime", LocalDateTime.of(2026, 4, 21, 12, 0));

        RegistrationRecord early = new RegistrationRecord(student, earlySection, LocalDateTime.now());
        RegistrationRecord late = new RegistrationRecord(student, lateSection, LocalDateTime.now());
        RegistrationRecord noSection = new RegistrationRecord();

        assertTrue(early.compareTo(late) < 0);
        assertTrue(late.compareTo(early) > 0);
        assertTrue(early.compareTo(noSection) < 0);
        assertNull(noSection.getStartTime());
        assertNull(noSection.getEndTime());
        assertEquals(13, early.addCredits(10));
    }

    @Test
    void registrationPlan_AddAndRemoveEntry_UpdatesBidirectionalLink() {
        Student student = buildStudent(10);
        RegistrationPlan plan = new RegistrationPlan(student, "2026A", 1);
        Section section = buildSection(buildCourse("CS520", 3), 30);
        PlanEntry entry = new PlanEntry(plan, section, PlanEntry.EntryType.SELECTED);

        plan.addEntry(entry);
        assertEquals(1, plan.getEntries().size());
        assertSame(plan, entry.getPlan());

        plan.removeEntry(entry);
        assertTrue(plan.getEntries().isEmpty());
        assertNull(entry.getPlan());

        plan.setApplyStatus(RegistrationPlan.ApplyStatus.APPLIED);
        plan.setApplySummary("ok");
        assertEquals(RegistrationPlan.ApplyStatus.APPLIED, plan.getApplyStatus());
        assertEquals("ok", plan.getApplySummary());
    }

    @Test
    void planEntry_AndRegistrationPeriod_GettersAndSetters_Work() {
        RegistrationPlan plan = new RegistrationPlan();
        plan.setPlanId(7);
        Section section = buildSection(buildCourse("CS530", 3), 10);

        PlanEntry entry = new PlanEntry();
        entry.setEntryId(11);
        entry.setPlan(plan);
        entry.setSection(section);
        entry.setEntryType(PlanEntry.EntryType.WAITLIST);
        entry.setStatus(PlanEntry.EntryStatus.PENDING);
        entry.setJoinWaitlistOnAddFailure(true);

        assertEquals(11, entry.getEntryId());
        assertSame(plan, entry.getPlan());
        assertSame(section, entry.getSection());
        assertEquals(PlanEntry.EntryType.WAITLIST, entry.getEntryType());
        assertEquals(PlanEntry.EntryStatus.PENDING, entry.getStatus());
        assertTrue(entry.isJoinWaitlistOnAddFailure());

        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);
        RegistrationPeriod period = new RegistrationPeriod(2024, start, end, "2026A");
        period.setPeriodId(9);
        period.setCohort(2025);
        period.setStartDateTime(start.plusDays(1));
        period.setEndDateTime(end.minusDays(1));
        period.setTerm("2026B");

        assertEquals(9, period.getPeriodId());
        assertEquals(2025, period.getCohort());
        assertEquals(start.plusDays(1), period.getStartDateTime());
        assertEquals(end.minusDays(1), period.getEndDateTime());
        assertEquals("2026B", period.getTerm());
    }

    @Test
    void adminUserAndInstructor_BuilderPaths_AndUnsupportedMethods() {
        Admin admin = new Admin.AdminBuilder()
                .withStaffId(42)
                .withUserEID("e001")
                .withName("Admin User")
                .withPassword("pw")
                .build();
        assertEquals(42, admin.getStaffId());
        assertEquals("e001", admin.getUserEID());
        assertEquals("Admin User", admin.getUserName());
        assertEquals("pw", admin.getPassword());

        User instructorUser = new Instructor.InstructorBuilder()
                .withStaffId(8)
                .withDepartment("COMP")
                .withUserEID("i001")
                .withName("Instructor")
                .withPassword("pw")
                .build();

        assertTrue(instructorUser instanceof Instructor);
        Instructor instructor = (Instructor) instructorUser;
        assertEquals("COMP", instructor.getDepartment());
        assertThrows(UnsupportedOperationException.class, instructor::getStaffId);
        assertThrows(UnsupportedOperationException.class, instructor::getTimeTable);
    }
}