package org;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationOperation;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class RegistrationOperationTest {
    @Test
    void RegistrationOperationTest() {
        RegistrationOperation rp = new RegistrationOperation();
        assertNotNull(rp);

    }
}
