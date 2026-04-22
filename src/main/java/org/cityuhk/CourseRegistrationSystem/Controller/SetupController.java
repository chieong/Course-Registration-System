package org.cityuhk.CourseRegistrationSystem.Controller;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class SetupController {

    private final AdminRepositoryPort adminRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(AdminRepositoryPort adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/setup")
    public String setupPage() {
        if (adminRepository.count() > 0) {
            return "redirect:/login";
        }
        return "setup";
    }

    @PostMapping("/setup")
    public String createInitialAdmin(
            @RequestParam String userEID,
            @RequestParam String name,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        if (adminRepository.count() > 0) {
            return "redirect:/login";
        }
        if (userEID == null || userEID.isBlank()
                || name == null || name.isBlank()
                || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("setupError", "All fields are required.");
            return "redirect:/setup";
        }

        String normalizedUserEID = userEID.trim();
        if (adminRepository.findByUserEID(normalizedUserEID).isPresent()) {
            redirectAttributes.addFlashAttribute("setupError", "User EID already exists.");
            return "redirect:/setup";
        }

        Admin admin =
                (Admin)
                        new Admin.AdminBuilder()
                                .withUserEID(normalizedUserEID)
                                .withName(name.trim())
                                .withPassword(passwordEncoder.encode(password))
                                .build();
        adminRepository.save(admin);
        redirectAttributes.addFlashAttribute("setupSuccess", "Initial admin account created.");
        return "redirect:/login";
    }
}