package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.List;

import org.cityuhk.CourseRegistrationSystem.Controller.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrativeService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministrativeService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Admin> listUsers() {
        return adminRepository.findAll();
    }

    @Transactional
    public Admin createUser(AdminUserRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        Admin admin = (Admin) new Admin.AdminBuilder()
                .withUserEID(request.getUserEID())
                .withName(request.getName())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .build();

        return adminRepository.save(admin);
    }

    @Transactional
    public Admin modifyUser(Integer staffId, AdminUserRequest request) {
        Admin existingAdmin = adminRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        String encodedPassword = existingAdmin.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        Admin updatedAdmin = (Admin) new Admin.AdminBuilder()
                .withStaffId(existingAdmin.getStaffId())
                .withUserEID(request.getUserEID())
                .withName(request.getName())
                .withPassword(encodedPassword)
                .build();

        return adminRepository.save(updatedAdmin);
    }

    @Transactional
    public void removeUser(Integer staffId) {
        if (!adminRepository.existsById(staffId)) {
            throw new RuntimeException("Admin user not found");
        }
        adminRepository.deleteById(staffId);
    }
}