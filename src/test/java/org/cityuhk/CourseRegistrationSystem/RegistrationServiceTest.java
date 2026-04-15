package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.RegistrationRecordRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private RegistrationRecordRepository registrationRecordRepository;
    @InjectMocks private RegistrationService registrationService;

    private final Integer studentId = 1;
    private final Integer sectionId = 1;
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Test
    public void testAddSection_Success() {
        Student student = mock(Student.class);
        Section section = mock(Section.class);
        RegistrationRecord record = mock(RegistrationRecord.class);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        when(registrationRecordRepository.countEnrolled(sectionId)).thenReturn(0);
        when(student.addSection(eq(section), any(LocalDateTime.class), anyInt())).thenReturn(record);
        registrationService.addSection(studentId, sectionId, timestamp);
        verify(registrationRecordRepository).save(record);
        verify(student).addSection(eq(section), eq(timestamp), eq(0));
    }

    @Test
    public void testAddSection_AlreadyEnrolled() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mock(Student.class)));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(mock(Section.class)));
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(true);
        assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
    }

    @Test
    public void testAddSection_StudentNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
    }

    @Test
    public void testAddSection_SectionNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mock(Student.class)));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> registrationService.addSection(studentId, sectionId, timestamp));
    }

    @Test
    public void testDropSection_Success() {
        Student student = mock(Student.class);
        Section section = mock(Section.class);
        RegistrationRecord record = mock(RegistrationRecord.class);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(true);
        when(student.dropSection(eq(section), any(LocalDateTime.class))).thenReturn(record);
        registrationService.dropSection(studentId, sectionId, timestamp);
        verify(registrationRecordRepository).delete(record);
        verify(student).dropSection(eq(section), eq(timestamp));
    }

    @Test
    public void testDropSection_NotEnrolled() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mock(Student.class)));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(mock(Section.class)));
        when(registrationRecordRepository.exists(studentId, sectionId)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
    }

    @Test
    public void testDropSection_StudentNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
    }

    @Test
    public void testDropSection_SectionNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mock(Student.class)));
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> registrationService.dropSection(studentId, sectionId, timestamp));
    }

    @Test
    public void testDeleteStudent() {
        registrationService.deleteStudent(studentId);
        verify(studentRepository).deleteById(studentId);
    }
}