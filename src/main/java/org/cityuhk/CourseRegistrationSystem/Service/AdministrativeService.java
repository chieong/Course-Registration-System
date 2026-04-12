package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrativeService {

    private final AdminRepository adminRepository;

    public AdministrativeService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Transactional
    public Admin createUser(String userEID, String name) {
        Admin admin = (Admin) new Admin.AdminBuilder()
                .withUserEID(userEID)
                .withName(name)
                .build();
        return adminRepository.save(admin);
    }

    @Transactional
    public Admin modifyUser(Integer staffId, String userEID, String name) {
        Admin existingAdmin = adminRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        Admin updatedAdmin = (Admin) new Admin.AdminBuilder()
                .withStaffId(existingAdmin.getStaffId())
                .withUserEID(userEID)
                .withName(name)
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