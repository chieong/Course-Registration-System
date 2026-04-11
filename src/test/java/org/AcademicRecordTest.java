package org;

import org.cityuhk.CourseRegistrationSystem.Service.AcademicRecord;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class AcademicRecordTest {
    @Test
    void AcademicRecordTest() {
        AcademicRecord ar = new AcademicRecord();
        assertNotNull(ar);
    }
}
