package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationPlan;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class RegistrationPlanTest {
    @Test
    void RegistrationPlanTest() {
        RegistrationPlan rp = new RegistrationPlan();
        assertNotNull(rp);

    }

}
