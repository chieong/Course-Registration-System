package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import org.cityuhk.CourseRegistrationSystem.Exception.UserEidAlreadyExistsException;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class GlobalUserEidUniquenessPolicy implements UserEidUniquenessPolicy {

    private final AdminRepositoryPort adminRepository;
    private final StudentRepositoryPort studentRepository;
    private final InstructorRepositoryPort instructorRepository;

    public GlobalUserEidUniquenessPolicy(
            AdminRepositoryPort adminRepository,
            StudentRepositoryPort studentRepository,
            InstructorRepositoryPort instructorRepository) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    public void assertUnique(String eid, Integer excludeAdminId, Integer excludeStudentId, Integer excludeInstructorId) {
        adminRepository.findByUserEID(eid).ifPresent(admin -> {
            if (excludeAdminId == null || !excludeAdminId.equals(admin.getStaffId())) {
                throw new UserEidAlreadyExistsException(eid);
            }
        });

        studentRepository.findByUserEID(eid).ifPresent(student -> {
            if (excludeStudentId == null || !excludeStudentId.equals(student.getStudentId())) {
                throw new UserEidAlreadyExistsException(eid);
            }
        });

        instructorRepository.findByUserEID(eid).ifPresent(instructor -> {
            if (excludeInstructorId == null || !excludeInstructorId.equals(instructor.getStaffId())) {
                throw new UserEidAlreadyExistsException(eid);
            }
        });
    }
}
