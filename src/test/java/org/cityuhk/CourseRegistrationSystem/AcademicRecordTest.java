package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Model.AcademicRecord;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class AcademicRecordTest {
    @Test
    public void AcademicRecordTest() {
        AcademicRecord ar = new AcademicRecord();
        assertNotNull(ar);
    }
}
