package org.cityuhk.CourseRegistrationSystem.Service.Administrative;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationPeriodRepository;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodValidatorTest {

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @InjectMocks
    private RegistrationPeriodValidator validator;

    private AdminPeriodRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AdminPeriodRequest();
        validRequest.setCohort(2024);
        validRequest.setTerm("2026A");
        validRequest.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        validRequest.setEndDate(LocalDateTime.of(2026, 11, 30, 23, 59));
    }

    @Test
    void validate_nullCohort_throws() {
        validRequest.setCohort(null);
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_nullTerm_throws() {
        validRequest.setTerm(null);
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_blankTerm_throws() {
        validRequest.setTerm("   ");
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_nullStartDate_throws() {
        validRequest.setStartDate(null);
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_nullEndDate_throws() {
        validRequest.setEndDate(null);
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_startAfterEnd_throws() {
        validRequest.setStartDate(LocalDateTime.of(2026, 12, 1, 0, 0));
        validRequest.setEndDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_startEqualsEnd_throws() {
        LocalDateTime same = LocalDateTime.of(2026, 9, 1, 0, 0);
        validRequest.setStartDate(same);
        validRequest.setEndDate(same);
        assertThrows(RegistrationPeriodValidationException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_overlap_throws() {
        LocalDateTime s = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime e = LocalDateTime.of(2026, 11, 30, 23, 59);
        RegistrationPeriod existing = new RegistrationPeriod(2024, s, e, "2026A");
        when(registrationPeriodRepository.findOverlappingPeriods(anyInt(), any(), any()))
                .thenReturn(List.of(existing));
        assertThrows(RegistrationPeriodOverlapException.class, () -> validator.validate(validRequest));
    }

    @Test
    void validate_noOverlap_succeeds() {
        when(registrationPeriodRepository.findOverlappingPeriods(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> validator.validate(validRequest));
    }
}
