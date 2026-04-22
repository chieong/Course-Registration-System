package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvCourseRepository implements CourseRepositoryPort {

    static final String FILE = "courses.csv";
    static final String[] HEADER = {
            "courseId", "courseCode", "title", "credits", "description",
            "prerequisiteCourseIds", "exclusiveCourseIds"
    };

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;

    public CsvCourseRepository(CsvFileStore store, CsvIdGenerator idGen) {
        this.store = store;
        this.idGen = idGen;
    }

    List<Course> loadAll() {
        // First pass: load raw data
        List<String[]> rawRows = store.readRows(FILE);
        Map<Integer, Course> byId = new LinkedHashMap<>();
        Map<Integer, String> prereqIds = new HashMap<>();
        Map<Integer, String> exclusiveIds = new HashMap<>();

        for (String[] row : rawRows) {
            if (row.length < 7) continue;
            try {
                int courseId = Integer.parseInt(row[0]);
                Course c = new Course(row[1], row[2],
                        Integer.parseInt(row[3]), row[4],
                        new HashSet<>(), new HashSet<>(), null);
                c.setCourseId(courseId);
                byId.put(courseId, c);
                prereqIds.put(courseId, row[5]);
                exclusiveIds.put(courseId, row[6]);
            } catch (NumberFormatException ignored) {
            }
        }

        // Second pass: resolve relationships
        for (Map.Entry<Integer, Course> entry : byId.entrySet()) {
            Set<Course> prereqs = resolveIds(entry.getValue().getCourseId(), prereqIds, byId);
            Set<Course> exclusives = resolveIds(entry.getValue().getCourseId(), exclusiveIds, byId);
            entry.getValue().setPrerequisiteCourses(prereqs);
            entry.getValue().setExclusiveCourses(exclusives);
        }
        return new ArrayList<>(byId.values());
    }

    private Set<Course> resolveIds(int ownId, Map<Integer, String> idsMap, Map<Integer, Course> byId) {
        Set<Course> result = new HashSet<>();
        String raw = idsMap.getOrDefault(ownId, "");
        if (raw == null || raw.isBlank()) return result;
        for (String part : raw.split(";")) {
            try {
                int id = Integer.parseInt(part.trim());
                Course c = byId.get(id);
                if (c != null) result.add(c);
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private synchronized void saveAll(List<Course> courses) {
        List<String[]> rows = courses.stream().map(c -> new String[]{
                String.valueOf(c.getCourseId()),
                safe(c.getCourseCode()),
                safe(c.getTitle()),
                String.valueOf(c.getCredits()),
                safe(c.getDescription()),
                joinIds(c.getPrerequisiteCourses()),
                joinIds(c.getExclusiveCourses())
        }).collect(Collectors.toList());
        store.writeRows(FILE, HEADER, rows);
    }

    private String joinIds(Set<Course> courses) {
        if (courses == null || courses.isEmpty()) return "";
        return courses.stream()
                .map(c -> String.valueOf(c.getCourseId()))
                .collect(Collectors.joining(";"));
    }

    private static String safe(String v) { return v == null ? "" : v; }

    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        return loadAll().stream()
                .filter(c -> c.getCourseCode() != null && c.getCourseCode().equals(courseCode))
                .findFirst();
    }

    @Override
    public boolean existsByCourseCode(String courseCode) {
        return findByCourseCode(courseCode).isPresent();
    }

    @Override
    public Course getCourseByCourseCode(String courseCode) {
        return findByCourseCode(courseCode).orElse(null);
    }

    @Override
    public synchronized Course save(Course course) {
        List<Course> all = loadAll();
        if (course.getCourseId() == null) {
            course.setCourseId(idGen.nextId("course"));
            all.add(course);
        } else {
            Integer id = course.getCourseId();
            all.removeIf(c -> Objects.equals(c.getCourseId(), id));
            all.add(course);
        }
        saveAll(all);
        return course;
    }

    @Override
    public synchronized void delete(Course course) {
        List<Course> all = loadAll();
        all.removeIf(c -> Objects.equals(c.getCourseId(), course.getCourseId()));
        saveAll(all);
    }

    @Override
    public List<Course> findAll() {
        return loadAll();
    }
}
