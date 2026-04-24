package org.cityuhk.CourseRegistrationSystem.Service.UserTest;

import org.cityuhk.CourseRegistrationSystem.Exception.*;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminInstructorRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.GlobalUserEidUniquenessPolicy;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.InstructorUserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorUserManagementServiceTest {

    @Mock
    private InstructorRepositoryPort instructorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GlobalUserEidUniquenessPolicy eidPolicy;

    @InjectMocks
    private InstructorUserManagementService service;

    private AdminInstructorRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AdminInstructorRequest();
        validRequest.setUserEID(" i001 ");
        validRequest.setName(" Dr Smith ");
        validRequest.setPassword("pw");
        validRequest.setDepartment("CS");
    }

    // ---------- listInstructors ----------

    @Test
    void listInstructors_ReturnsAllInstructors() {
        Instructor i1 = mock(Instructor.class);
        Instructor i2 = mock(Instructor.class);

        when(instructorRepository.findAll()).thenReturn(List.of(i1, i2));

        List<Instructor> result = service.listInstructors();

        assertEquals(2, result.size());
    }

    // ---------- createInstructor ----------

    @Test
    void createInstructor_WhenValid_CreatesInstructor() {
        when(passwordEncoder.encode("pw")).thenReturn("ENCODED");
        doNothing().when(eidPolicy).assertUnique("i001", null, null, null);
        when(instructorRepository.save(any(Instructor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instructor created = service.createInstructor(validRequest);

        assertEquals("i001", created.getUserEID());
        assertEquals("Dr Smith", created.getUserName());
        assertEquals("ENCODED", created.getPassword());
        assertEquals("CS", created.getDepartment());
    }

    @Test
    void createInstructor_WhenUserEIDInvalid_ThrowsException() {
        validRequest.setUserEID(" ");

        assertThrows(InvalidUserEIDException.class,
                () -> service.createInstructor(validRequest));
    }

    @Test
    void createInstructor_WhenNameInvalid_ThrowsException() {
        validRequest.setName(" ");

        assertThrows(InvalidNameException.class,
                () -> service.createInstructor(validRequest));
    }

    @Test
    void createInstructor_WhenPasswordInvalid_ThrowsException() {
        validRequest.setPassword("");

        assertThrows(InvalidPasswordException.class,
                () -> service.createInstructor(validRequest));
    }

    // ---------- modifyInstructor ----------

    @Test
    void modifyInstructor_WhenValid_ReplacesFields() {
        Instructor existing = new Instructor.InstructorBuilder()
                .withStaffId(10)
                .withUserEID("i001")
                .withName("Old Name")
                .withPassword("OLDPWD")
                .withDepartment("CS")
                .build();

        when(instructorRepository.findByUserEID("i001"))
                .thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("pw")).thenReturn("NEWPWD");
        doNothing().when(eidPolicy).assertUnique("i001", null, null, 10);
        when(instructorRepository.save(any(Instructor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instructor updated = service.modifyInstructor(validRequest);

        assertEquals("Dr Smith", updated.getUserName());
        assertEquals("NEWPWD", updated.getPassword());
        assertEquals(10, updated.getStaffId());
    }

    @Test
    void modifyInstructor_WhenOptionalFieldsNull_UsesExistingValues() {
        Instructor existing = new Instructor.InstructorBuilder()
                .withStaffId(20)
                .withUserEID("i002")
                .withName("Existing")
                .withPassword("EXISTPWD")
                .withDepartment("Math")
                .build();

        AdminInstructorRequest request = new AdminInstructorRequest();
        request.setUserEID("i002"); // other fields null

        when(instructorRepository.findByUserEID("i002"))
                .thenReturn(Optional.of(existing));
        doNothing().when(eidPolicy).assertUnique("i002", null, null, 20);
        when(instructorRepository.save(any(Instructor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instructor updated = service.modifyInstructor(request);

        assertEquals("Existing", updated.getUserName());
        assertEquals("EXISTPWD", updated.getPassword());
        assertEquals("Math", updated.getDepartment());
    }

    @Test
    void modifyInstructor_WhenInstructorNotFound_ThrowsException() {
        when(instructorRepository.findByUserEID("i001"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.modifyInstructor(validRequest));
    }

    @Test
    void modifyInstructor_WhenUserEIDInvalid_ThrowsException() {
        validRequest.setUserEID(" ");

        assertThrows(InvalidUserEIDException.class,
                () -> service.modifyInstructor(validRequest));
    }

    // ---------- removeInstructor ----------

    @Test
    void removeInstructor_WhenExists_DeletesInstructor() {
        Instructor instructor = new Instructor.InstructorBuilder()
                .withStaffId(30)
                .withUserEID("i003")
                .build();

        when(instructorRepository.findByUserEID("i003"))
                .thenReturn(Optional.of(instructor));

        service.removeInstructor(" i003 ");

        verify(instructorRepository).deleteById(30);
    }

    @Test
    void removeInstructor_WhenNotFound_ThrowsException() {
        when(instructorRepository.findByUserEID("i004"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.removeInstructor("i004"));
    }

    @Test
    void createInstructor_WhenAllFieldsValid_FallsThroughValidationIfs() {
        when(passwordEncoder.encode("pw")).thenReturn("ENCODED");
        doNothing().when(eidPolicy).assertUnique("i001", null, null, null);
        when(instructorRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instructor instructor = service.createInstructor(validRequest);

        assertEquals("i001", instructor.getUserEID());
        assertEquals("Dr Smith", instructor.getUserName());
    }

    @Test
    void modifyInstructor_WhenUserEIDPresent_PassesInitialValidation() {
        Instructor existing = new Instructor.InstructorBuilder()
                .withStaffId(1)
                .withUserEID("i001")
                .withName("Old")
                .withPassword("OLDPWD")
                .withDepartment("CS")
                .build();

        when(instructorRepository.findByUserEID("i001"))
                .thenReturn(Optional.of(existing));
        doNothing().when(eidPolicy).assertUnique("i001", null, null, 1);
        when(instructorRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instructor updated = service.modifyInstructor(validRequest);

        assertEquals("Dr Smith", updated.getUserName());
    }

    @Test
    void modifyInstructor_WhenPasswordIsBlank_DoesNotReencodePassword() {
        Instructor existing = new Instructor.InstructorBuilder()
                .withStaffId(2)
                .withUserEID("i002")
                .withName("Existing")
                .withPassword("OLDPWD")
                .withDepartment("CS")
                .build();

        AdminInstructorRequest request = new AdminInstructorRequest();
        request.setUserEID("i002");
        request.setPassword("   "); // non-null BUT blank

        when(instructorRepository.findByUserEID("i002"))
                .thenReturn(Optional.of(existing));
        doNothing().when(eidPolicy).assertUnique("i002", null, null, 2);
        when(instructorRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instructor updated = service.modifyInstructor(request);

        assertEquals("OLDPWD", updated.getPassword()); // fallback path
    }
    }

