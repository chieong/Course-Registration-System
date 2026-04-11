package org;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationRecord;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class RegistrationRecordTest {
    @Test
    void RegistrationRecordTest() {
        RegistrationRecord rp = new RegistrationRecord();
        assertNotNull(rp);

    }
}
