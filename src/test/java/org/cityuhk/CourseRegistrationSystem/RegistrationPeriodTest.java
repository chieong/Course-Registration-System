package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class RegistrationPeriodTest {
    @Test
    void registrationPeriodTest() { // registrationPeriodCanBeInstantiated()
        RegistrationPeriod rp = new RegistrationPeriod();
        assertNotNull(rp);
}
}
