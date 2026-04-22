package org.cityuhk.CourseRegistrationSystem.Cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CliSessionTest {

    @Test
    void constructorAndGettersWork() {
        CliSession session = new CliSession("s123456", CliRole.STUDENT);

        assertEquals("s123456", session.getUserEid());
        assertEquals(CliRole.STUDENT, session.getRole());
    }

    @Test
    void cliRoleEnumValuesExist() {
        assertEquals(CliRole.ADMIN, CliRole.valueOf("ADMIN"));
        assertEquals(CliRole.STUDENT, CliRole.valueOf("STUDENT"));
    }
}
