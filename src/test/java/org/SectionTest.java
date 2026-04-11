package org;

import org.cityuhk.CourseRegistrationSystem.Service.Section;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class SectionTest {

    @Test
    void getSectionIDTest() {
        Section section = new Section(9, 30, 0, 10, "LEC", "Y4704", null, 0, null, null);
        assertEquals(9, section.getSectionID());
    }

    @Test
    void enrollStudentThrowsWhenNotImplemented() {
        Section section = new Section(9, 30, 0, 10, "LEC", "Y4704", null, 0, null, null);
        assertThrows(UnsupportedOperationException.class, () -> section.enrollStudent(null));
    }

    @Test
    void waitlistStudentThrowsWhenNotImplemented() {
        Section section = new Section(9, 30, 0, 10, "LEC", "Y4704", null, 0, null, null);
        assertThrows(UnsupportedOperationException.class, () -> section.waitlistStudent(null));
    }
}
