package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserManagementService implements AdminUserManagementOperations {

    private final AdminRepositoryPort adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalUserEidUniquenessPolicy eidPolicy;

    public AdminUserManagementService(
            AdminRepositoryPort adminRepository,
            PasswordEncoder passwordEncoder,
            GlobalUserEidUniquenessPolicy eidPolicy) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.eidPolicy = eidPolicy;
    }

    public List<Admin> listUsers() {
        return adminRepository.findAll();
    }

    public Admin createUser(AdminUserRequest request) {
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

        Admin admin = new Admin.AdminBuilder()
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .build();

        return adminRepository.save(admin);
    }

    public Admin modifyUser(AdminUserRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new InvalidUserEIDException();
        }
        String normalizedUserEID = request.getUserEID().trim();
        Admin existingAdmin = adminRepository.findByUserEID(normalizedUserEID)
                .orElseThrow(() -> new UserNotFoundException("Admin", normalizedUserEID));

        // Unique check
        String newUserEID = (request.getUserEID() != null) ? request.getUserEID().trim() : existingAdmin.getUserEID();
        eidPolicy.assertUnique(newUserEID, existingAdmin.getStaffId(), null, null);

        // Password Fallback
        String encodedPassword = existingAdmin.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        // Name Fallback
        String updatedName = (request.getName() != null) ? request.getName().trim() : existingAdmin.getUserName();

        Admin updatedAdmin = new Admin.AdminBuilder()
                .withStaffId(existingAdmin.getStaffId())
                .withUserEID(newUserEID)
                .withName(updatedName)
                .withPassword(encodedPassword)
                .build();

        return adminRepository.save(updatedAdmin);
    }

    public void removeUser(String userEID) {
        String normalizedUserEID = userEID.trim();
        Admin admin = adminRepository.findByUserEID(normalizedUserEID)
                .orElseThrow(() -> new UserNotFoundException("Admin", normalizedUserEID));
        adminRepository.deleteById(admin.getStaffId());
    }
}
