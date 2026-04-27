package org.cityuhk.CourseRegistrationSystem.Service.UserTest;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminUserRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.AdminUserManagementService;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.GlobalUserEidUniquenessPolicy;
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
class AdminUserManagementServiceTest {

    @Mock
    private AdminRepositoryPort adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GlobalUserEidUniquenessPolicy eidPolicy;

    @InjectMocks
    private AdminUserManagementService service;

    private AdminUserRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AdminUserRequest();
        validRequest.setUserEID(" a001 ");
        validRequest.setName(" Alice ");
        validRequest.setPassword("pw");
    }

    @Test
    void listUsers_ReturnsAllAdmins() {
        Admin a1 = mock(Admin.class);
        Admin a2 = mock(Admin.class);

        when(adminRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Admin> result = service.listUsers();

        assertEquals(2, result.size());
    }

    @Test
    void createUser_WhenValid_CreatesAdmin() {
        when(passwordEncoder.encode("pw")).thenReturn("ENCODED");
        doNothing().when(eidPolicy).assertUnique("a001", null, null, null);
        when(adminRepository.save(any(Admin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Admin created = service.createUser(validRequest);

        assertEquals("a001", created.getUserEID());
        assertEquals("Alice", created.getUserName());
        assertEquals("ENCODED", created.getPassword());
    }

    @Test
    void createUser_WhenUserEIDNull_ThrowsException() {
        validRequest.setUserEID(null);

        assertThrows(InvalidUserEIDException.class,
                () -> service.createUser(validRequest));
    }

    @Test
    void createUser_WhenUserEIDBlank_ThrowsException() {
        validRequest.setUserEID("   ");

        assertThrows(InvalidUserEIDException.class,
                () -> service.createUser(validRequest));
    }

    @Test
    void createUser_WhenNameNull_ThrowsException() {
        validRequest.setName(null);

        assertThrows(InvalidNameException.class,
                () -> service.createUser(validRequest));
    }

    @Test
    void createUser_WhenNameBlank_ThrowsException() {
        validRequest.setName("   ");

        assertThrows(InvalidNameException.class,
                () -> service.createUser(validRequest));
    }

    @Test
    void createUser_WhenPasswordNull_ThrowsException() {
        validRequest.setPassword(null);

        assertThrows(InvalidPasswordException.class,
                () -> service.createUser(validRequest));
    }

    @Test
    void createUser_WhenPasswordBlank_ThrowsException() {
        validRequest.setPassword("   ");

        assertThrows(InvalidPasswordException.class,
                () -> service.createUser(validRequest));
    }

    @Test
    void modifyUser_WhenValid_ReplacesFields() {
        Admin existing = new Admin.AdminBuilder()
                .withStaffId(1)
                .withUserEID("a001")
                .withName("Old Name")
                .withPassword("OLDPWD")
                .build();

        when(adminRepository.findByUserEID("a001"))
                .thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("pw")).thenReturn("NEWPWD");
        doNothing().when(eidPolicy).assertUnique("a001", 1, null, null);
        when(adminRepository.save(any(Admin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Admin updated = service.modifyUser(validRequest);

        assertEquals("Alice", updated.getUserName());
        assertEquals("NEWPWD", updated.getPassword());
    }

    @Test
    void modifyUser_WhenOptionalFieldsNull_UsesExistingValues() {
        Admin existing = new Admin.AdminBuilder()
                .withStaffId(2)
                .withUserEID("a002")
                .withName("Existing")
                .withPassword("EXISTPWD")
                .build();

        AdminUserRequest request = new AdminUserRequest();
        request.setUserEID("a002"); // other fields null

        when(adminRepository.findByUserEID("a002"))
                .thenReturn(Optional.of(existing));
        doNothing().when(eidPolicy).assertUnique("a002", 2, null, null);
        when(adminRepository.save(any(Admin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Admin updated = service.modifyUser(request);

        assertEquals("Existing", updated.getUserName());
        assertEquals("EXISTPWD", updated.getPassword());
    }

    @Test
    void modifyUser_WhenPasswordBlank_UsesExistingPassword() {
        Admin existing = new Admin.AdminBuilder()
                .withStaffId(3)
                .withUserEID("a003")
                .withName("Admin")
                .withPassword("OLDPWD")
                .build();

        AdminUserRequest request = new AdminUserRequest();
        request.setUserEID("a003");
        request.setPassword("   "); // non-null but blank

        when(adminRepository.findByUserEID("a003"))
                .thenReturn(Optional.of(existing));
        doNothing().when(eidPolicy).assertUnique("a003", 3, null, null);
        when(adminRepository.save(any(Admin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Admin updated = service.modifyUser(request);

        assertEquals("OLDPWD", updated.getPassword());
    }

    @Test
    void modifyUser_WhenUserEIDInvalid_ThrowsException() {
        validRequest.setUserEID("   ");

        assertThrows(InvalidUserEIDException.class,
                () -> service.modifyUser(validRequest));
    }

    @Test
    void modifyUser_WhenAdminNotFound_ThrowsException() {
        when(adminRepository.findByUserEID("a001"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.modifyUser(validRequest));
    }

    @Test
    void removeUser_WhenExists_DeletesAdmin() {
        Admin admin = new Admin.AdminBuilder()
                .withStaffId(10)
                .withUserEID("a010")
                .build();

        when(adminRepository.findByUserEID("a010"))
                .thenReturn(Optional.of(admin));

        service.removeUser(" a010 ");

        verify(adminRepository).deleteById(10);
    }

    @Test
    void removeUser_WhenNotFound_ThrowsException() {
        when(adminRepository.findByUserEID("a011"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.removeUser("a011"));
    }

    @Test
void modifyUser_WhenNameBlank_UsesTrimmedEmptyString() {
    Admin existing = new Admin.AdminBuilder()
            .withStaffId(4)
            .withUserEID("a004")
            .withName("Original")
            .withPassword("PWD")
            .build();

    AdminUserRequest request = new AdminUserRequest();
    request.setUserEID("a004");
    request.setName("   ");     // blank but not null
    request.setPassword(null);  // skip password branch

    when(adminRepository.findByUserEID("a004"))
            .thenReturn(Optional.of(existing));
    doNothing().when(eidPolicy).assertUnique("a004", 4, null, null);
    when(adminRepository.save(any(Admin.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Admin updated = service.modifyUser(request);

    assertEquals("", updated.getUserName());
}
    
}

