package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;

public class GlobalUserEidUniquenessPolicy {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public GlobalUserEidUniquenessPolicy(
            AdminRepository adminRepository,
            StudentRepository studentRepository,
            InstructorRepository instructorRepository) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    public void assertUnique(String eid, Integer excludeAdminId, Integer excludeStudentId, Integer excludeInstructorId) {
        adminRepository.findByUserEID(eid).ifPresent(admin -> {
            if (excludeAdminId == null || !excludeAdminId.equals(admin.getStaffId())) {
                throw new RuntimeException("User EID already exists");
            }
        });

        studentRepository.findByUserEID(eid).ifPresent(student -> {
            if (excludeStudentId == null || !excludeStudentId.equals(student.getStudentId())) {
                throw new RuntimeException("User EID already exists");
            }
        });

        instructorRepository.findByUserEID(eid).ifPresent(instructor -> {
            if (excludeInstructorId == null || !excludeInstructorId.equals(instructor.getStaffId())) {
                throw new RuntimeException("User EID already exists");
            }
        });
    }
}
