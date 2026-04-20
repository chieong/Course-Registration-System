package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;

import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Repository.InstructorRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.InstructorUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

public class InstructorUserManagementService {

    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalUserEidUniquenessPolicy eidPolicy;

    public InstructorUserManagementService(
            InstructorRepository instructorRepository,
            PasswordEncoder passwordEncoder,
            GlobalUserEidUniquenessPolicy eidPolicy) {
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
        this.eidPolicy = eidPolicy;
    }

    public List<Instructor> listInstructors() {
        return instructorRepository.findAll();
    }

    public Instructor createInstructor(InstructorUserRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new RuntimeException("User EID is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        String normalizedUserEID = request.getUserEID().trim();
        eidPolicy.assertUnique(normalizedUserEID, null, null, null);

        Instructor instructor = new Instructor.InstructorBuilder()
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .withDepartment(request.getDepartment())
                .build();

        return instructorRepository.save(instructor);
    }

    public Instructor modifyInstructor(Integer staffId, InstructorUserRequest request) {
        Instructor existing = instructorRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new RuntimeException("User EID is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        String normalizedUserEID = request.getUserEID().trim();
        eidPolicy.assertUnique(normalizedUserEID, null, null, existing.getStaffId());

        String encodedPassword = existing.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        Instructor updated = new Instructor.InstructorBuilder()
                .withStaffId(existing.getStaffId())
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(encodedPassword)
                .withDepartment(request.getDepartment() != null ? request.getDepartment() : existing.getDepartment())
                .build();

        return instructorRepository.save(updated);
    }

    public void removeInstructor(Integer staffId) {
        if (!instructorRepository.existsById(staffId)) {
            throw new RuntimeException("Instructor not found");
        }
        instructorRepository.deleteById(staffId);
    }
}
