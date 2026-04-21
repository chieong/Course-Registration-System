package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.StudentRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvStudentRepository implements StudentRepositoryPort {

    static final String FILE = "students.csv";
    static final String[] HEADER = {
            "studentId", "userEID", "name", "password",
            "minSemesterCredit", "maxSemesterCredit", "major",
            "cohort", "department", "maxDegreeCredit"
    };

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;

    public CsvStudentRepository(CsvFileStore store, CsvIdGenerator idGen) {
        this.store = store;
        this.idGen = idGen;
    }

    List<Student> loadAll() {
        List<Student> students = new ArrayList<>();
        for (String[] row : store.readRows(FILE)) {
            if (row.length < 10) continue;
            try {
                Student s = new Student.StudentBuilder()
                        .withStudentId(Integer.parseInt(row[0]))
                        .withUserEID(row[1])
                        .withName(row[2])
                        .withPassword(row[3])
                        .withMinSemesterCredit(Integer.parseInt(row[4]))
                        .withMaxSemesterCredit(Integer.parseInt(row[5]))
                        .withMajor(row[6])
                        .withCohort(Integer.parseInt(row[7]))
                        .withDepartment(row[8])
                        .withMaxDegreeCredit(Integer.parseInt(row[9]))
                        .build();
                students.add(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return students;
    }

    synchronized void saveAll(List<Student> students) {
        List<String[]> rows = students.stream()
                .map(s -> new String[]{
                        String.valueOf(s.getStudentId()),
                        safe(s.getUserEID()),
                        safe(s.getUserName()),
                        safe(s.getPassword()),
                        String.valueOf(s.getMinSemesterCredit()),
                        String.valueOf(s.getMaxSemesterCredit()),
                        safe(s.getMajor()),
                        String.valueOf(s.getCohort()),
                        safe(s.getDepartment()),
                        String.valueOf(s.getMaxDegreeCredit())
                })
                .collect(Collectors.toList());
        store.writeRows(FILE, HEADER, rows);
    }

    private static String safe(String v) { return v == null ? "" : v; }

    @Override
    public Optional<Student> findByUserEID(String userEID) {
        return loadAll().stream()
                .filter(s -> s.getUserEID() != null && s.getUserEID().equalsIgnoreCase(userEID))
                .findFirst();
    }

    @Override
    public Optional<Student> findById(Integer id) {
        return loadAll().stream()
                .filter(s -> Objects.equals(s.getStudentId(), id))
                .findFirst();
    }

    public synchronized Student save(Student student) {
        List<Student> all = loadAll();
        if (student.getStudentId() == null || student.getStudentId() == 0) {
            int newId = idGen.nextId("student");
            student = new Student.StudentBuilder()
                    .withStudentId(newId)
                    .withUserEID(student.getUserEID())
                    .withName(student.getUserName())
                    .withPassword(student.getPassword())
                    .withMinSemesterCredit(student.getMinSemesterCredit())
                    .withMaxSemesterCredit(student.getMaxSemesterCredit())
                    .withMajor(student.getMajor())
                    .withCohort(student.getCohort())
                    .withDepartment(student.getDepartment())
                    .withMaxDegreeCredit(student.getMaxDegreeCredit())
                    .build();
            all.add(student);
        } else {
            Integer id = student.getStudentId();
            all.removeIf(s -> Objects.equals(s.getStudentId(), id));
            all.add(student);
        }
        saveAll(all);
        return student;
    }
}
