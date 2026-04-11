package org;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.cityuhk.CourseRegistrationSystem.Service.LoginSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class LoginSessionTest {

    @Test
    void getSessionIdTest() {
        LoginSession session = new LoginSession("user1");
        assertNotNull(session.getSessionId());
    }

    @Test
    void getUserEIDTest() {
        LoginSession session = new LoginSession("user1");
        assertEquals("user1", session.getUserEID());
    }

    @Test
    void isExpiredTest() {
        LoginSession session = new LoginSession("user1");
        assertThrows(NullPointerException.class, () -> session.isExpired());
    }

    @Test
    void isExpiredReturnsTrueWhenValidUntilIsInPast() throws Exception {
        LoginSession session = new LoginSession("user1");

        Field validUtilField = LoginSession.class.getDeclaredField("validUtil");
        validUtilField.setAccessible(true);
        validUtilField.set(session, LocalDateTime.now().minusMinutes(1));

        assertTrue(session.isExpired());
    }

    @Test
    void isExpiredReturnsFalseWhenValidUntilIsInFuture() throws Exception {
        LoginSession session = new LoginSession("user1");

        Field validUtilField = LoginSession.class.getDeclaredField("validUtil");
        validUtilField.setAccessible(true);
        validUtilField.set(session, LocalDateTime.now().plusMinutes(1));

        assertFalse(session.isExpired());
    }
}
