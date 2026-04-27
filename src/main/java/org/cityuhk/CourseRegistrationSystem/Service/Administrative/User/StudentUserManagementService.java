package org.cityuhk.CourseRegistrationSystem.Service.Administrative.User;

import java.util.List;
import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Exception.InvalidNameException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidPasswordException;
import org.cityuhk.CourseRegistrationSystem.Exception.InvalidUserEIDException;
import org.cityuhk.CourseRegistrationSystem.Exception.UserNotFoundException;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.RestController.dto.AdminStudentRequest;
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

    public Student createStudent(AdminStudentRequest request) {
        if (request.getUserEID() == null || request.getUserEID().isBlank()) {
            throw new InvalidUserEIDException();
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidNameException();
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidPasswordException();
        }
        if (request.getCohort() == null) {
            throw new RuntimeException("No cohort provided");
        }
        if (request.getDepartment() == null || request.getDepartment().isBlank()) {
            throw new RuntimeException("No department provided");
        }
        if (request.getMaxSemesterCredit() == null) {
            throw new RuntimeException("No maximum semester credit provided");
        }
        if (request.getMinSemesterCredit() == null) {
            throw new RuntimeException("No minimum semester credit provided");
        }
        if (request.getMaxDegreeCredit() == null) {
            throw new RuntimeException("No maximum degree credit provided");
        }


        String normalizedUserEID = request.getUserEID().trim();
        eidPolicy.assertUnique(normalizedUserEID, null, null, null);

        Student student = new Student.StudentBuilder()
                .withUserEID(normalizedUserEID)
                .withName(request.getName().trim())
                .withPassword(passwordEncoder.encode(request.getPassword()))
                .withMajor(request.getMajor())
                .withDepartment(request.getDepartment())
                .withCohort(request.getCohort())
                .withMaxSemesterCredit(request.getMaxSemesterCredit())
                .withMinSemesterCredit(request.getMinSemesterCredit())
                .withMaxDegreeCredit(request.getMaxDegreeCredit())
                .build();

        return studentRepository.save(student);
    }

    public Student modifyStudent(AdminStudentRequest request) {
        String normalizedUserEID = request.getUserEID().trim();
        Student existing = studentRepository.findByUserEID(normalizedUserEID)
                .orElseThrow(() -> new UserNotFoundException("Student", normalizedUserEID));

        Student updated = new Student.StudentBuilder()
                .withStudentId(existing.getStudentId())
                .withUserEID(normalizedUserEID)

                .withName(request.getName() != null ? request.getName().trim() : existing.getUserName())
                .withPassword(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : existing.getPassword())

                .withMajor(request.getMajor() != null ? request.getMajor() : existing.getMajor())
                .withDepartment(request.getDepartment() != null ? request.getDepartment() : existing.getDepartment())

                .withMinSemesterCredit(request.getMinSemesterCredit() != null ? request.getMinSemesterCredit() : existing.getMinSemesterCredit())
                .withMaxSemesterCredit(request.getMaxSemesterCredit() != null ? request.getMaxSemesterCredit() : existing.getMaxSemesterCredit())
                .withMaxDegreeCredit(request.getMaxDegreeCredit() != null ? request.getMaxDegreeCredit() : existing.getMaxDegreeCredit())
                .withCohort(request.getCohort() != null ? request.getCohort() : existing.getCohort())

                .build();

        return studentRepository.save(updated);
    }

    public void removeStudent(String userEID) {
        Optional<Student> student = studentRepository.findByUserEID(userEID);
        if (student.isEmpty()) {
            throw new UserNotFoundException("Student", userEID);
        }
        studentRepository.deleteById(student.get().getStudentId());
    }
}
