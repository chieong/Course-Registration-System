package org.cityuhk.CourseRegistrationSystem.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class LoginController {

    // Hardcoded users: username -> [password, role]
    private static final Map<String, String[]> USERS = Map.of(
        "student1",  new String[]{"student123",  "STUDENT"},
        "teacher1",  new String[]{"teacher123",  "TEACHER"},
        "admin1",    new String[]{"admin123",     "ADMIN"}
    );

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session) {
        String[] credentials = USERS.get(username);
        if (credentials != null && credentials[0].equals(password)) {
            session.setAttribute("username", username);
            session.setAttribute("role", credentials[1]);
            return "redirect:/";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
