package org.cityuhk.CourseRegistrationSystem;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Model.Instructor;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.InstructorRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUsersInitializer implements CommandLineRunner {

    private final StudentRepositoryPort studentRepository;
    private final AdminRepositoryPort adminRepository;
    private final InstructorRepositoryPort instructorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DefaultUsersInitializer(StudentRepositoryPort studentRepository,
                                   AdminRepositoryPort adminRepository,
                                   InstructorRepositoryPort instructorRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) {
        seedAdmin("admin", "Admin", "admin123");
        seedAdmin("kobyashi", "Kobyashi", "kobyashi123");

        seedStudent("tomori01", "Tomori Takamatsu", "tomori123", "Literature", 2024, "Humanities", 12, 18, 120);
        seedStudent("anon01", "Anon Chihaya", "anon123", "International Studies", 2024, "Social Sciences", 15, 21, 128);
        seedStudent("rana01", "Raana Kaname", "rana123", "Music Performance", 2024, "Arts", 9, 15, 120);
        seedStudent("soyo01", "Soyo Nagasaki", "soyo123", "Political Science", 2024, "Social Sciences", 12, 18, 130);
        seedStudent("taki01", "Taki Shiina", "taki123", "Music Production", 2024, "Arts", 12, 18, 120);

        seedInstructor("sakiko", "Sakiko Togawa", "saki123", "Classical Composition");
        seedInstructor("mutsumi", "Mutsumi Wakaba", "mutsumi123","Botany & Agriculture");
        seedInstructor("uika", "Uika Misumi", "uika123", "Performing Arts");
        seedInstructor("nyamu", "Nyamu Yuutenji", "nyamu123", "Digital Media & Marketing");
        seedInstructor("umiri", "Umiri Yahata", "umiri123", "Computer Science");
    }

    private void seedStudent(String eid, String name, String rawPassword, String major, int cohort, String department, int minSemesterCredit, int maxSemesterCredit, int maxDegreeCredit) {
        if (studentRepository.findByUserEID(eid).isPresent()) return;

        Student u = new Student.StudentBuilder()
                .withUserEID(eid)
                .withName(name)
                .withPassword(passwordEncoder.encode(rawPassword))
                .withMajor(major)
                .withCohort(cohort)
                .withDepartment(department)
                .withMinSemesterCredit(minSemesterCredit)
                .withMaxSemesterCredit(maxSemesterCredit)
                .withMaxDegreeCredit(maxDegreeCredit)
                .build();

        studentRepository.save(u);
    }

    private void seedInstructor(String eid, String name, String rawPassword, String department) {
        if (instructorRepository.findByUserEID(eid).isPresent()) return;

        Instructor i = new Instructor.InstructorBuilder()
                .withUserEID(eid)
                .withName(name)
                .withPassword(passwordEncoder.encode(rawPassword))
                .withDepartment(department)
                .build();

        instructorRepository.save(i);
    }

    private void seedAdmin(String eid, String name, String rawPassword) {
        if (adminRepository.findByUserEID(eid).isPresent()) return;

        Admin a = new Admin.AdminBuilder()
                .withUserEID(eid)
                .withName(name)
                .withPassword(passwordEncoder.encode(rawPassword))
                .build();

        adminRepository.save(a);
    }
}