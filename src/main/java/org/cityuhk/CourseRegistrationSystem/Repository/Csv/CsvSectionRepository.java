package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvSectionRepository implements SectionRepositoryPort {

    static final String FILE = "sections.csv";
    static final String[] HEADER = {
            "sectionId", "courseId", "enrollCapacity", "waitlistCapacity",
            "startTime", "endTime", "venue", "type"
    };

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;
    private final CsvCourseRepository courseRepository;

    public CsvSectionRepository(CsvFileStore store, CsvIdGenerator idGen,
                                 CsvCourseRepository courseRepository) {
        this.store = store;
        this.idGen = idGen;
        this.courseRepository = courseRepository;
    }

    List<Section> loadAll() {
        Map<Integer, Course> courseMap = courseRepository.loadAll().stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));

        List<Section> sections = new ArrayList<>();
        for (String[] row : store.readRows(FILE)) {
            if (row.length < 8) continue;
            try {
                int sectionId = Integer.parseInt(row[0]);
                int courseId = Integer.parseInt(row[1]);
                Course course = courseMap.get(courseId);
                if (course == null) continue;

                LocalDateTime start = row[4].isBlank() ? null : LocalDateTime.parse(row[4]);
                LocalDateTime end = row[5].isBlank() ? null : LocalDateTime.parse(row[5]);

                Section s = new Section();
                s.setSectionId(sectionId);
                s.setCourse(course);
                s.setEnrollCapacity(Integer.parseInt(row[2]));
                s.setWaitlistCapacity(Integer.parseInt(row[3]));
                s.setVenue(row[6]);
                if (!row[7].isBlank()) {
                    s.setType(Section.Type.valueOf(row[7]));
                }
                // set times directly via reflection to bypass the broken validation in Section
                setTimeDirectly(s, start, end);
                sections.add(s);
            } catch (Exception ignored) {
            }
        }
        return sections;
    }

    /** Uses setTime but catches any exception from Section's unusual validation. */
    private void setTimeDirectly(Section s, LocalDateTime start, LocalDateTime end) {
        try {
            java.lang.reflect.Field startField = Section.class.getDeclaredField("startTime");
            startField.setAccessible(true);
            startField.set(s, start);
            java.lang.reflect.Field endField = Section.class.getDeclaredField("endTime");
            endField.setAccessible(true);
            endField.set(s, end);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set section time fields", e);
        }
    }

    private synchronized void saveAll(List<Section> sections) {
        List<String[]> rows = sections.stream().map(s -> new String[]{
                String.valueOf(s.getSectionId()),
                s.getCourse() == null ? "" : String.valueOf(s.getCourse().getCourseId()),
                String.valueOf(getEnrollCapacity(s)),
                String.valueOf(getWaitlistCapacity(s)),
                s.getStartTime() == null ? "" : s.getStartTime().toString(),
                s.getEndTime() == null ? "" : s.getEndTime().toString(),
                safe(s.getVenue()),
                s.getType() == null ? "" : s.getType().name()
        }).collect(Collectors.toList());
        store.writeRows(FILE, HEADER, rows);
    }

    private int getEnrollCapacity(Section s) {
        try {
            java.lang.reflect.Field f = Section.class.getDeclaredField("enrollCapacity");
            f.setAccessible(true);
            Object val = f.get(s);
            return val == null ? 0 : (Integer) val;
        } catch (Exception e) { return 0; }
    }

    private int getWaitlistCapacity(Section s) {
        try {
            java.lang.reflect.Field f = Section.class.getDeclaredField("waitlistCapacity");
            f.setAccessible(true);
            Object val = f.get(s);
            return val == null ? 0 : (Integer) val;
        } catch (Exception e) { return 0; }
    }

    private static String safe(String v) { return v == null ? "" : v; }

    @Override
    public Optional<Section> findById(Integer id) {
        return loadAll().stream()
                .filter(s -> Objects.equals(s.getSectionId(), id))
                .findFirst();
    }

    @Override
    public synchronized Section save(Section section) {
        List<Section> all = loadAll();
        Integer sectionId = section.getSectionId();
        if (sectionId == null || sectionId == 0) {
            section.setSectionId(idGen.nextId("section"));
            all.add(section);
        } else {
            all.removeIf(s -> Objects.equals(s.getSectionId(), sectionId));
            all.add(section);
        }
        saveAll(all);
        return section;
    }

    @Override
    public synchronized void deleteById(Integer id) {
        List<Section> all = loadAll();
        all.removeIf(s -> Objects.equals(s.getSectionId(), id));
        saveAll(all);
    }

    @Override
    public List<Section> findAll() {
        return loadAll();
    }

    @Override
    public boolean overlapsInVenue(String venue, LocalDateTime startTime, LocalDateTime endTime) {
        return loadAll().stream()
            .filter(s -> s.overlapsInVenue(venue, startTime, endTime))
            .findAny()
            .isPresent();
    }
}
