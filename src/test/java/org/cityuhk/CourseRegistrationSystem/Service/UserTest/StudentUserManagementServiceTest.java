package org.cityuhk.CourseRegistrationSystem.Service.UserTest;

import org.cityuhk.CourseRegistrationSystem.Exception.*;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminStudentRequest;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.GlobalUserEidUniquenessPolicy;
import org.cityuhk.CourseRegistrationSystem.Service.Administrative.User.StudentUserManagementService;
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
class StudentUserManagementServiceTest {

    @Mock
    private StudentRepositoryPort studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GlobalUserEidUniquenessPolicy eidPolicy;

    @InjectMocks
    private StudentUserManagementService service;

    private AdminStudentRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AdminStudentRequest();
        validRequest.setUserEID(" s001 ");
        validRequest.setName(" Alice ");
        validRequest.setPassword("pw");
        validRequest.setCohort(2024);
        validRequest.setDepartment("CS");
        validRequest.setMajor("CS");
        validRequest.setMinSemesterCredit(0);
        validRequest.setMaxSemesterCredit(18);
        validRequest.setMaxDegreeCredit(120);
    }

    // ---------- listStudents ----------

    @Test
    void listStudents_ReturnsAllStudents() {
        Student s1 = new Student.StudentBuilder()
                .withStudentId(1)
                .withUserEID("s001")
                .withName("Alice")
                .withPassword("pw")
                .withDepartment("CS")
                .withCohort(2024)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMaxDegreeCredit(120)
                .build();

        Student s2 = new Student.StudentBuilder()
                .withStudentId(2)
                .withUserEID("s002")
                .withName("Bob")
                .withPassword("pw")
                .withDepartment("CS")
                .withCohort(2024)
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMaxDegreeCredit(120)
                .build();

        when(studentRepository.findAll()).thenReturn(List.of(s1, s2));

        List<Student> students = service.listStudents();

        assertEquals(2, students.size());
    }

    // ---------- createStudent (success) ----------

    @Test
    void createStudent_WhenValid_CreatesAndSavesStudent() {
        when(passwordEncoder.encode("pw")).thenReturn("ENCODED");
        doNothing().when(eidPolicy).assertUnique("s001", null, null, null);
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student created = service.createStudent(validRequest);

        assertEquals("s001", created.getUserEID());
        assertEquals("Alice", created.getUserName());
        assertEquals("ENCODED", created.getPassword());

        verify(studentRepository).save(any(Student.class));
    }

    // ---------- createStudent (validation failures) ----------

    @Test
    void createStudent_WhenUserEIDInvalid_ThrowsException() {
        validRequest.setUserEID(" ");

        assertThrows(InvalidUserEIDException.class,
                () -> service.createStudent(validRequest));
    }

    @Test
    void createStudent_WhenNameInvalid_ThrowsException() {
        validRequest.setName("");

        assertThrows(InvalidNameException.class,
                () -> service.createStudent(validRequest));
    }

    @Test
    void createStudent_WhenPasswordInvalid_ThrowsException() {
        validRequest.setPassword(" ");

        assertThrows(InvalidPasswordException.class,
                () -> service.createStudent(validRequest));
    }

    @Test
    void createStudent_WhenCohortMissing_ThrowsException() {
        validRequest.setCohort(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createStudent(validRequest));

        assertEquals("No cohort provided", ex.getMessage());
    }

    @Test
    void createStudent_WhenDepartmentMissing_ThrowsException() {
        validRequest.setDepartment(" ");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createStudent(validRequest));

        assertEquals("No department provided", ex.getMessage());
    }

    // ---------- modifyStudent ----------

    @Test
    void modifyStudent_WhenStudentExists_UpdatesAndSaves() {
        Student existing = new Student.StudentBuilder()
                .withStudentId(1)
                .withUserEID("s001")
                .withName("Old Name")
                .withPassword("OLD")
                .withDepartment("CS")
                .withCohort(2024)
                .withMaxSemesterCredit(18)
                .withMinSemesterCredit(0)
                .withMaxDegreeCredit(120)
                .build();

        when(studentRepository.findByUserEID("s001"))
                .thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("pw")).thenReturn("NEWPWD");
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student updated = service.modifyStudent(validRequest);

        assertEquals("Alice", updated.getUserName());
        assertEquals("NEWPWD", updated.getPassword());
        assertEquals(1, updated.getStudentId());
    }

    @Test
    void modifyStudent_WhenStudentNotFound_ThrowsException() {
        when(studentRepository.findByUserEID("s001"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.modifyStudent(validRequest));
    }

    // ---------- removeStudent ----------

    @Test
    void removeStudent_WhenExists_DeletesStudent() {
        Student student = new Student.StudentBuilder()
                .withStudentId(1)
                .withUserEID("s001")
                .build();

        when(studentRepository.findByUserEID("s001"))
                .thenReturn(Optional.of(student));

        service.removeStudent("s001");

        verify(studentRepository).deleteById(1);
    }

    @Test
    void removeStudent_WhenNotFound_ThrowsException() {
        when(studentRepository.findByUserEID("s001"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.removeStudent("s001"));
    }

    @Test
        void createStudent_WhenMaxSemesterCreditMissing_ThrowsException() {
        validRequest.setMaxSemesterCredit(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createStudent(validRequest));

        assertEquals("No maximum semester credit provided", ex.getMessage());
        }

        @Test
        void createStudent_WhenMinSemesterCreditMissing_ThrowsException() {
        validRequest.setMinSemesterCredit(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createStudent(validRequest));

        assertEquals("No minimum semester credit provided", ex.getMessage());
        }

        @Test
        void createStudent_WhenMaxDegreeCreditMissing_ThrowsException() {
        validRequest.setMaxDegreeCredit(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createStudent(validRequest));

        assertEquals("No maximum degree credit provided", ex.getMessage());
        }

        @Test
        void modifyStudent_WhenOptionalFieldsNull_UsesExistingValues() {
        Student existing = new Student.StudentBuilder()
                .withStudentId(1)
                .withUserEID("s001")
                .withName("Old Name")
                .withPassword("OLDPWD")
                .withMajor("CS")
                .withDepartment("CS")
                .withMinSemesterCredit(0)
                .withMaxSemesterCredit(18)
                .withMaxDegreeCredit(120)
                .withCohort(2024)
                .build();

        AdminStudentRequest request = new AdminStudentRequest();
        request.setUserEID("s001");
        // leave ALL optional fields null

        when(studentRepository.findByUserEID("s001"))
                .thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student updated = service.modifyStudent(request);

        assertEquals("Old Name", updated.getUserName());
        assertEquals("OLDPWD", updated.getPassword());
        assertEquals("CS", updated.getMajor());
        assertEquals("CS", updated.getDepartment());
        assertEquals(0, updated.getMinSemesterCredit());
        assertEquals(18, updated.getMaxSemesterCredit());
        assertEquals(120, updated.getMaxDegreeCredit());
        assertEquals(2024, updated.getCohort());
        }


}
