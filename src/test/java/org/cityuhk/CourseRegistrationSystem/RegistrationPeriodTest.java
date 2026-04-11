package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Service.RegistrationPeriod;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class RegistrationPeriodTest {
    @Test
    void registrationPeriodCanBeInstantiated() {
        RegistrationPeriod rp = new RegistrationPeriod();
        assertNotNull(rp);
}
}
