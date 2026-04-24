package org.cityuhk.CourseRegistrationSystem.Service.UserTest;

import org.cityuhk.CourseRegistrationSystem.Exception.UserEidAlreadyExistsException;
import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.GlobalUserEidUniquenessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalUserEidUniquenessPolicyTest {

    @Mock
    private AdminRepositoryPort adminRepository;

    @Mock
    private StudentRepositoryPort studentRepository;

    @Mock
    private InstructorRepositoryPort instructorRepository;

    private GlobalUserEidUniquenessPolicy policy;

    private static final String EID = "abc123";

    @BeforeEach
    void setUp() {
        policy = new GlobalUserEidUniquenessPolicy(
                adminRepository,
                studentRepository,
                instructorRepository
        );
    }

    // =========================================================
    // ✅ No user exists anywhere → PASS
    // =========================================================
    @Test
    void assertUnique_WhenEidDoesNotExistAnywhere_Passes() {
        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID(EID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                policy.assertUnique(EID, null, null, null));
    }

    // =========================================================
    // ✅ Admin exists, NOT excluded → THROW
    // =========================================================
    @Test
    void assertUnique_WhenAdminExistsAndNotExcluded_ThrowsException() {
        Admin admin = mock(Admin.class);

        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.of(admin));

        assertThrows(UserEidAlreadyExistsException.class, () ->
                policy.assertUnique(EID, null, null, null));
    }

    // =========================================================
    // ✅ Admin exists, excluded → PASS
    // =========================================================
    @Test
    void assertUnique_WhenAdminExistsButExcluded_Passes() {
        Admin admin = mock(Admin.class);
        when(admin.getStaffId()).thenReturn(10);

        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.of(admin));
        when(studentRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID(EID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                policy.assertUnique(EID, 10, null, null));
    }

    // =========================================================
    // ✅ Student exists, NOT excluded → THROW
    // =========================================================
    @Test
    void assertUnique_WhenStudentExistsAndNotExcluded_ThrowsException() {
        Student student = mock(Student.class);

        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID(EID)).thenReturn(Optional.of(student));

        assertThrows(UserEidAlreadyExistsException.class, () ->
                policy.assertUnique(EID, null, null, null));
    }

    // =========================================================
    // ✅ Student exists, excluded → PASS
    // =========================================================
    @Test
    void assertUnique_WhenStudentExistsButExcluded_Passes() {
        Student student = mock(Student.class);
        when(student.getStudentId()).thenReturn(30);

        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID(EID)).thenReturn(Optional.of(student));
        when(instructorRepository.findByUserEID(EID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                policy.assertUnique(EID, null, 30, null));
    }

    // =========================================================
    // ✅ Instructor exists, NOT excluded → THROW
    // =========================================================
    @Test
    void assertUnique_WhenInstructorExistsAndNotExcluded_ThrowsException() {
        Instructor instructor = mock(Instructor.class);

        when(studentRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID(EID)).thenReturn(Optional.of(instructor));

        assertThrows(UserEidAlreadyExistsException.class, () ->
                policy.assertUnique(EID, null, null, null));
    }

    // =========================================================
    // ✅ Instructor exists, excluded → PASS
    // =========================================================
    @Test
    void assertUnique_WhenInstructorExistsButExcluded_Passes() {
        Instructor instructor = mock(Instructor.class);
        when(instructor.getStaffId()).thenReturn(50);

        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(studentRepository.findByUserEID(EID)).thenReturn(Optional.empty());
        when(instructorRepository.findByUserEID(EID)).thenReturn(Optional.of(instructor));

        assertDoesNotThrow(() ->
                policy.assertUnique(EID, null, null, 50));
    }

    // =========================================================
    // ✅ Admin excluded BUT Student exists → THROW
    // =========================================================
    @Test
    void assertUnique_WhenAdminExcludedButStudentExists_ThrowsException() {
        Admin admin = mock(Admin.class);
        when(admin.getStaffId()).thenReturn(1);

        Student student = mock(Student.class);

        when(adminRepository.findByUserEID(EID)).thenReturn(Optional.of(admin));
        when(admin.getStaffId()).thenReturn(3);

        assertThrows(UserEidAlreadyExistsException.class, () ->
                policy.assertUnique(EID, 1, null, null));
    }
}

