package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminInstructorRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class InstructorUserManagementService implements InstructorUserManagementOperations {

    private final InstructorRepositoryPort instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalUserEidUniquenessPolicy eidPolicy;

    public InstructorUserManagementService(
            InstructorRepositoryPort instructorRepository,
            PasswordEncoder passwordEncoder,
            GlobalUserEidUniquenessPolicy eidPolicy) {
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
        this.eidPolicy = eidPolicy;
    }

    public List<Instructor> listInstructors() {
        return instructorRepository.findAll();
    }

    public Instructor createInstructor(AdminInstructorRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new InvalidUserEIDException();
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidNameException();
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidPasswordException();
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

    public Instructor modifyInstructor(AdminInstructorRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new InvalidUserEIDException();
        }
        String normalizedUserEID = request.getUserEID().trim();
        Instructor existing = instructorRepository.findByUserEID(normalizedUserEID)
                .orElseThrow(() -> new UserNotFoundException("Instructor", normalizedUserEID));

        // Logic: If user didn't change the EID, we use the normalized version of the old one
        String newUserEID = (request.getUserEID() != null) ? request.getUserEID().trim() : existing.getUserEID();
        eidPolicy.assertUnique(newUserEID, null, null, existing.getStaffId());

        // Password Fallback
        String encodedPassword = existing.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        // Name Fallback (Fixes the NullPointerException)
        String updatedName = (request.getName() != null) ? request.getName().trim() : existing.getUserName();

        Instructor updated = new Instructor.InstructorBuilder()
                .withStaffId(existing.getStaffId())
                .withUserEID(newUserEID)
                .withName(updatedName)
                .withPassword(encodedPassword)
                .withDepartment(request.getDepartment() != null ? request.getDepartment() : existing.getDepartment())
                .build();

        return instructorRepository.save(updated);
    }

    public void removeInstructor(String userEID) {
        String normalizedUserEID = userEID.trim();
        Optional<Instructor> existing = instructorRepository.findByUserEID(normalizedUserEID);
        if (existing.isEmpty()) {
            throw new UserNotFoundException("Instructor", normalizedUserEID);
        }
        instructorRepository.deleteById(existing.get().getStaffId());
    }
}
