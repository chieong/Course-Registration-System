package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Service.Registration.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrationRestControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationRestController controller;

    @Test
    void joinWaitlist_success_returns200() {
        ResponseEntity<?> response = controller.joinWaitlist(1, 10);

        verify(registrationService).waitListSection(eq(1), eq(10), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void joinWaitlist_alreadyWaitlisted_returns400() {
        doThrow(new RuntimeException("Already waitlisted"))
                .when(registrationService).waitListSection(eq(1), eq(10), any());

        ResponseEntity<?> response = controller.joinWaitlist(1, 10);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Already waitlisted", response.getBody());
    }

    @Test
    void joinWaitlist_notEligible_returns400() {
        doThrow(new RuntimeException("Student not eligible to register"))
                .when(registrationService).waitListSection(eq(1), eq(10), any());

        ResponseEntity<?> response = controller.joinWaitlist(1, 10);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Student not eligible to register", response.getBody());
    }

    @Test
    void joinWaitlist_studentNotFound_returns400() {
        doThrow(new RuntimeException("Student not found"))
                .when(registrationService).waitListSection(eq(2), eq(10), any());

        ResponseEntity<?> response = controller.joinWaitlist(2, 10);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Student not found", response.getBody());
    }
}
