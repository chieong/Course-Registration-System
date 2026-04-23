package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationRecord;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationRecordRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvRegistrationRecordRepository implements RegistrationRecordRepositoryPort {

    static final String FILE = "registration_records.csv";
    static final String[] HEADER = {"recordId", "studentId", "sectionId", "timestamp"};

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;
    private final CsvStudentRepository studentRepository;
    private final CsvSectionRepository sectionRepository;

    public CsvRegistrationRecordRepository(CsvFileStore store, CsvIdGenerator idGen,
                                            CsvStudentRepository studentRepository,
                                            CsvSectionRepository sectionRepository) {
        this.store = store;
        this.idGen = idGen;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
    }

    private List<RegistrationRecord> loadAll() {
        Map<Integer, Student> studentMap = studentRepository.loadAll().stream()
                .collect(Collectors.toMap(Student::getStudentId, s -> s));
        Map<Integer, Section> sectionMap = sectionRepository.loadAll().stream()
                .collect(Collectors.toMap(Section::getSectionId, s -> s));

        List<RegistrationRecord> records = new ArrayList<>();
        for (String[] row : store.readRows(FILE)) {
            if (row.length < 4) continue;
            try {
                int recordId = Integer.parseInt(row[0]);
                int studentId = Integer.parseInt(row[1]);
                int sectionId = Integer.parseInt(row[2]);
                LocalDateTime ts = row[3].isBlank() ? null : LocalDateTime.parse(row[3]);

                Student student = studentMap.get(studentId);
                Section section = sectionMap.get(sectionId);
                if (student == null || section == null) continue;

                RegistrationRecord rr = new RegistrationRecord(student, section, ts);
                rr.setRecordId(recordId);
                records.add(rr);
            } catch (Exception ignored) {
            }
        }
        return records;
    }

    private synchronized void saveAll(List<RegistrationRecord> records) {
        List<String[]> rows = records.stream().map(r -> new String[]{
                String.valueOf(r.getRecordId()),
                String.valueOf(r.getStudent().getStudentId()),
                String.valueOf(r.getSection().getSectionId()),
                r.getTimestamp() == null ? "" : r.getTimestamp().toString()
        }).collect(Collectors.toList());
        store.writeRows(FILE, HEADER, rows);
    }

    @Override
    public int countEnrolled(Integer sectionId) {
        return (int) loadAll().stream()
                .filter(r -> r.getSection().getSectionId() == sectionId)
                .count();
    }

    @Override
    public List<RegistrationRecord> findAllRecords() {
        return loadAll();
    }

    @Override
    public boolean exists(Integer studentId, Integer sectionId) {
        return loadAll().stream().anyMatch(r ->
                Objects.equals(r.getStudent().getStudentId(), studentId)
                        && r.getSection().getSectionId() == sectionId);
    }

    @Override
    public Optional<RegistrationRecord> findByStudentIdAndSectionId(Integer studentId, Integer sectionId) {
        return loadAll().stream()
                .filter(r -> Objects.equals(r.getStudent().getStudentId(), studentId)
                        && r.getSection().getSectionId() == sectionId)
                .findFirst();
    }

    @Override
    public List<RegistrationRecord> find(Integer studentId, LocalDateTime start, LocalDateTime end) {
        return loadAll().stream()
                .filter(r -> Objects.equals(r.getStudent().getStudentId(), studentId))
                .filter(r -> r.getTimestamp() != null
                        && !r.getTimestamp().isBefore(start)
                        && !r.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationRecord> findByStudentId(Integer studentId) {
        return loadAll().stream()
                .filter(r -> Objects.equals(r.getStudent().getStudentId(), studentId))
                .sorted(Comparator.comparing(RegistrationRecord::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized RegistrationRecord save(RegistrationRecord record) {
        List<RegistrationRecord> all = loadAll();
        if (record.getRecordId() == null) {
            record.setRecordId(idGen.nextId("registration_record"));
            all.add(record);
        } else {
            Integer id = record.getRecordId();
            all.removeIf(r -> Objects.equals(r.getRecordId(), id));
            all.add(record);
        }
        saveAll(all);
        return record;
    }

    @Override
    public synchronized void delete(RegistrationRecord record) {
        List<RegistrationRecord> all = loadAll();
        all.removeIf(r -> Objects.equals(r.getRecordId(), record.getRecordId()));
        saveAll(all);
    }

    @Override
    public List<RegistrationRecord> findBySectionId(Integer sectionId) {
        return loadAll().stream()
                .filter(r -> Objects.equals(r.getSection().getSectionId(), sectionId))
                .sorted(Comparator.comparing(RegistrationRecord::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
}
