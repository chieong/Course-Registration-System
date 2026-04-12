package org.cityuhk.CourseRegistrationSystem.Controller;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Service.AdministrativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdministrativeRestController {

    private final AdministrativeService administrativeService;

    public AdministrativeRestController(AdministrativeService administrativeService) {
        this.administrativeService = administrativeService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody AdminUserRequest request) {
        try {
            Admin created = administrativeService.createUser(request.getUserEID(), request.getName());
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<?> modifyUser(@PathVariable Integer staffId, @RequestBody AdminUserRequest request) {
        try {
            Admin updated = administrativeService.modifyUser(staffId, request.getUserEID(), request.getName());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{staffId}")
    public ResponseEntity<?> removeUser(@PathVariable Integer staffId) {
        try {
            administrativeService.removeUser(staffId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    public static class AdminUserRequest {
        private String userEID;
        private String name;

        public String getUserEID() {
            return userEID;
        }

        public void setUserEID(String userEID) {
            this.userEID = userEID;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}