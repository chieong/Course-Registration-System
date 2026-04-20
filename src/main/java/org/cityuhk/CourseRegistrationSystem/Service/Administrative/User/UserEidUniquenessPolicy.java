package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

public interface UserEidUniquenessPolicy {
    void assertUnique(String eid, Integer excludeAdminId, Integer excludeStudentId, Integer excludeInstructorId);
}
