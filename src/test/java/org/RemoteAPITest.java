package org;

import org.cityuhk.CourseRegistrationSystem.Service.RemoteAPI;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class RemoteAPITest {

    @Test
    void getStudentRecordThrowsWhenNotImplemented() {
        RemoteAPI api = new RemoteAPI();
        assertThrows(UnsupportedOperationException.class, () -> api.getStudentRecord(1001));
    }
}
