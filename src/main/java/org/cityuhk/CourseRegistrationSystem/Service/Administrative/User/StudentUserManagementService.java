package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.StudentUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentUserManagementService implements StudentUserManagementOperations {

    private final StudentRepositoryPort studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalUserEidUniquenessPolicy eidPolicy;

    public StudentUserManagementService(
            StudentRepositoryPort studentRepository,
            PasswordEncoder passwordEncoder,
            GlobalUserEidUniquenessPolicy eidPolicy) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.eidPolicy = eidPolicy;
    }

    public List<Student> listStudents() {
        return studentRepository.findAll();
    }

    public Student createStudent(StudentUserRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new InvalidUserEIDException();
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidNameException();
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidPasswordException();
        }

        String normalizedUserEID = request.getUserEID().trim();
        eidPolicy.assertUnique(normalizedUserEID, null, null, null);

        Student student = new Student.StudentBuilder()
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .withMajor(request.getMajor())
                .withDepartment(request.getDepartment())
                .build();

        return studentRepository.save(student);
    }

    public Student modifyStudent(Integer studentId, StudentUserRequest request) {
        Student existing = studentRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Student", studentId));

        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new InvalidUserEIDException();
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidNameException();
        }

        String normalizedUserEID = request.getUserEID().trim();
        eidPolicy.assertUnique(normalizedUserEID, null, existing.getStudentId(), null);

        String encodedPassword = existing.getPassword();
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        Student updated = new Student.StudentBuilder()
                .withStudentId(existing.getStudentId())
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(encodedPassword)
                .withMajor(request.getMajor() != null ? request.getMajor() : existing.getMajor())
                .withDepartment(request.getDepartment() != null ? request.getDepartment() : existing.getDepartment())
                .withMinSemesterCredit(existing.getMinSemesterCredit())
                .withMaxSemesterCredit(existing.getMaxSemesterCredit())
                .withCohort(existing.getCohort())
                .withMaxDegreeCredit(existing.getMaxDegreeCredit())
                .build();

        return studentRepository.save(updated);
    }

    public void removeStudent(Integer studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new UserNotFoundException("Student", studentId);
        }
        studentRepository.deleteById(studentId);
    }
}
