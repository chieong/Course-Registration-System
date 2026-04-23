package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminPeriodRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.AdministrativeService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodOverlapException;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.RegistrationPeriodValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativeRestControllerTest {

    @Mock
    private AdministrativeService administrativeService;

    @InjectMocks
    private AdministrativeRestController controller;

    private RegistrationPeriod period;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 30, 23, 59);
        period = new RegistrationPeriod(2024, start, end);
        period.setPeriodId(1);
    }

    @Test
    void listPeriods_noCohortFilter_returnsAll() {
        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of(period));

        ResponseEntity<List<RegistrationPeriod>> response = controller.listPeriods(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void listPeriods_withCohortFilter_returnsFiltered() {
        when(administrativeService.listRegistrationPeriods(2024)).thenReturn(List.of(period));

        ResponseEntity<List<RegistrationPeriod>> response = controller.listPeriods(2024);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createPeriod_success_returnsUpdatedList() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        req.setCohort(2024);
        req.setStartDate(LocalDateTime.of(2026, 9, 1, 0, 0));
        req.setEndDate(LocalDateTime.of(2026, 11, 30, 23, 59));

        when(administrativeService.createRegistrationPeriod(any())).thenReturn(period);
        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of(period));

        ResponseEntity<?> response = controller.createPeriod(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<RegistrationPeriod> body = (List<RegistrationPeriod>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
    }

    @Test
    void createPeriod_validationException_returnsBadRequest() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        when(administrativeService.createRegistrationPeriod(any()))
                .thenThrow(new RegistrationPeriodValidationException("Cohort is required"));

        ResponseEntity<?> response = controller.createPeriod(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cohort is required", response.getBody());
    }

    @Test
    void createPeriod_overlapException_returnsBadRequest() {
        AdminPeriodRequest req = new AdminPeriodRequest();
        when(administrativeService.createRegistrationPeriod(any()))
                .thenThrow(new RegistrationPeriodOverlapException("Period overlaps with an existing period for cohort 2024"));

        ResponseEntity<?> response = controller.createPeriod(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Period overlaps with an existing period for cohort 2024", response.getBody());
    }

    @Test
    void deletePeriod_success_returnsUpdatedList() {
        when(administrativeService.listRegistrationPeriods(null)).thenReturn(List.of());

        ResponseEntity<?> response = controller.deletePeriod(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(administrativeService).deleteRegistrationPeriod(1);
    }

    @Test
    void deletePeriod_notFound_returnsBadRequest() {
        doThrow(new RegistrationPeriodValidationException("Registration period not found: 99"))
                .when(administrativeService).deleteRegistrationPeriod(99);

        ResponseEntity<?> response = controller.deletePeriod(99);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Registration period not found: 99", response.getBody());
    }
}
