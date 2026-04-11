package org;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.UUID;

import org.cityuhk.CourseRegistrationSystem.Service.CourseRegistrationSystem;
import org.cityuhk.CourseRegistrationSystem.Service.ICredentialRepository;
import org.cityuhk.CourseRegistrationSystem.Service.SessionManager;
import org.junit.jupiter.api.Test;

public class CourseRegistrationSystemTest {

    private CourseRegistrationSystem createSystem(boolean credentialValid) {
        ICredentialRepository repo = new ICredentialRepository() {
            @Override
            public boolean validateCredential(String userEID, String userPassword) {
                return credentialValid;
            }
        };
        SessionManager sessionManager = new SessionManager(repo);
        return new CourseRegistrationSystem(null, null, null, null, null, sessionManager);
    }

    @Test 
    void newSessionReturnsUuidWhenCredentialValid() {
        CourseRegistrationSystem system = createSystem(true);

        UUID sessionId = system.newSession("user1", "pw");
        assertNotNull(sessionId);
    }

    @Test
    void newSessionReturnsNullWhenCredentialInvalid() {
        CourseRegistrationSystem system = createSystem(false);

        UUID sessionId = system.newSession("user1", "wrong");
        assertNull(sessionId);
    }

    @Test
    void addSectionTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.addSection("CS101", 1, 100);
        });
    }

    @Test
    void dropSectionTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.dropSection("CS101", 1, 100);
        });
    }

    @Test
    void joinSectionWaitlistTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.joinSectionWaitlist("CS101", 1);
        });
    }

    @Test
    void submitPlanTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.submitPlan(new ArrayList<>(), new ArrayList<>(), 100);
        });
    }

    @Test
    void getPlanByStudentIdTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.getPlanByStudentId(100);
        });
    }

    @Test
    void getSectionListTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.getSectionList();
        });
    }

    @Test
    void checkTimeTableTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.checkTimeTable(100);
        });
    }

    @Test
    void checkStudentListTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.checkStudentList(1, 100);
        });
    }

    @Test
    void addCourseTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.addCourse("CS101", "Intro", 3, "desc", new ArrayList<>(), new ArrayList<>(), 100);
        });
    }

    @Test
    void modifyCourseTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.modifyCourse("CS101", "Intro", 3, "desc", new ArrayList<>(), new ArrayList<>(), 100);
        });
    }

    @Test
    void removeCourseTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.removeCourse("CS101", 100);
        });
    }

    @Test
    void setRegistrationPeriodTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.setRegistrationPeriod(2024, "2026-01-01 10:00", 123456, 100);
        });
    }

    @Test
    void removeRegistrationPeriodTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.removeRegistrationPeriod(1);
        });
    }

    @Test
    void modifyRegistrationPeriodTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.modifyRegistrationPeriod(1, 2024, "2026-01-01 10:00", 123456, 100);
        });
    }

    @Test
    void createNewStudentTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.createNewStudent("e123", "Student", 1001, 2024, "CS", "CS", 12, 18);
        });
    }

    @Test
    void modifyUserTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.modifyUser("e123", "Student", 1001, 2024, "CS", "CS", 12, 18);
        });
    }

    @Test
    void deleteUserTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.deleteUser("e123");
        });
    }

    @Test
    void isRegistrationPeriodOpenTest() {
        CourseRegistrationSystem system = createSystem(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            system.isRegistrationPeriodOpen(1001);
        });
    }
















}
