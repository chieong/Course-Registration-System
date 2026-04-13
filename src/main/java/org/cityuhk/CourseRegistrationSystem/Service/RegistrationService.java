package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

// Service class for managing Student entities.

@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final RegistrationRecordRepository registrationRecordRepository;

    @Autowired
    public RegistrationService(
            StudentRepository studentRepository,
            SectionRepository sectionRepository,
            RegistrationRecordRepository registrationRecordRepository) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.registrationRecordRepository = registrationRecordRepository;
    }

    // public Student saveStudent(Student student) {
    //     return studentRepository.save(student);
    // }
    //
    // public List<Student> getAllStudents() {
    //     return studentRepository.findAll();
    // }
    //
    // public Optional<Student> getStudentById(Integer id) {
    //     return studentRepository.findById(id);
    // }

    @Transactional
    public void addSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Optional<Student> existingStudent = studentRepository.findById(studentId);
        if (!existingStudent.isPresent()) {
            throw new RuntimeException("Student not found");
        }
        Optional<Section> existingSection = sectionRepository.findById(sectionId);
        if (!existingSection.isPresent()) {
            throw new RuntimeException("Section not found");
        }
        if (registrationRecordRepository.exists(studentId, sectionId)) {
            throw new RuntimeException("Already enrolled");
        }
        Student student = existingStudent.get();
        Section section = existingSection.get();
        int enrolled = registrationRecordRepository.countEnrolled(sectionId);
        registrationRecordRepository.save(student.addSection(section, timestamp, enrolled));
    }

    @Transactional
    public void dropSection(Integer studentId, Integer sectionId, LocalDateTime timestamp) {
        Optional<Student> existingStudent = studentRepository.findById(studentId);
        if (!existingStudent.isPresent()) {
            throw new RuntimeException("Student not found");
        }
        Optional<Section> existingSection = sectionRepository.findById(sectionId);
        if (!existingSection.isPresent()) {
            throw new RuntimeException("Section not found");
        }
        if (!registrationRecordRepository.exists(studentId, sectionId)) {
            throw new RuntimeException("Not enrolled");
        }
        Student student = existingStudent.get();
        Section section = existingSection.get();
        registrationRecordRepository.delete(student.dropSection(section, timestamp));
    }

    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }
}