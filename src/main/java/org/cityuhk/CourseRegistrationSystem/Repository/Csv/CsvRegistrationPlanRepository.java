package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPlan;
import org.cityuhk.CourseRegistrationSystem.Model.Student;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationPlanRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvRegistrationPlanRepository implements RegistrationPlanRepositoryPort {

    static final String FILE = "registration_plans.csv";
    static final String[] HEADER = {
            "planId", "studentId", "priority", "applyStatus", "applySummary"
    };

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;
    private final CsvStudentRepository studentRepository;

    public CsvRegistrationPlanRepository(CsvFileStore store, CsvIdGenerator idGen,
                                          CsvStudentRepository studentRepository) {
        this.store = store;
        this.idGen = idGen;
        this.studentRepository = studentRepository;
    }

    private List<RegistrationPlan> loadAll() {
        Map<Integer, Student> studentMap = studentRepository.loadAll().stream()
                .collect(Collectors.toMap(Student::getStudentId, s -> s));
        List<RegistrationPlan> plans = new ArrayList<>();
        for (String[] row : store.readRows(FILE)) {
            if (row.length < 5) continue;
            try {
                int planId = Integer.parseInt(row[0]);
                int studentId = Integer.parseInt(row[1]);
                Student student = studentMap.get(studentId);
                if (student == null) continue;
                RegistrationPlan plan = new RegistrationPlan(student, Integer.parseInt(row[2]));
                plan.setPlanId(planId);
                if (row[3] != null && !row[3].isBlank()) {
                    plan.setApplyStatus(RegistrationPlan.ApplyStatus.valueOf(row[3]));
                }
                plan.setApplySummary(row[4]);
                plans.add(plan);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return plans;
    }

     private synchronized void saveAll(List<RegistrationPlan> plans) {
         List<String[]> rows = plans.stream().map(p -> new String[]{
                 String.valueOf(p.getPlanId()),
                 String.valueOf(p.getStudent().getStudentId()),
                 String.valueOf(p.getPriority()),
                 safe(p.getApplyStatus().name()),
                 safe(p.getApplySummary())
         }).collect(Collectors.toList());
         store.writeRows(FILE, HEADER, rows);
     }

    private static String safe(String v) { return v == null ? "" : v; }

    /** Exposes loadAll for inter-repo use. */
    public List<RegistrationPlan> findAll() { return loadAll(); }

    @Override
    public Optional<RegistrationPlan> findById(Integer id) {
        return loadAll().stream().filter(p -> Objects.equals(p.getPlanId(), id)).findFirst();
    }

    @Override
    public List<RegistrationPlan> findByStudentId(Integer studentId) {
        return loadAll().stream()
                .filter(p -> Objects.equals(p.getStudent().getStudentId(), studentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationPlan> findByStudentIdOrderByPriorityAsc(Integer studentId) {
        return findByStudentId(studentId).stream()
                .sorted(Comparator.comparingInt(RegistrationPlan::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    public long countByStudentId(Integer studentId) {
        return findByStudentId(studentId).size();
    }

    @Override
    public synchronized RegistrationPlan save(RegistrationPlan plan) {
        List<RegistrationPlan> all = loadAll();
        if (plan.getPlanId() == null) {
            plan.setPlanId(idGen.nextId("registration_plan"));
            all.add(plan);
        } else {
            Integer id = plan.getPlanId();
            all.removeIf(p -> Objects.equals(p.getPlanId(), id));
            all.add(plan);
        }
        saveAll(all);
        return plan;
    }

    @Override
    public synchronized void deleteById(Integer id) {
        List<RegistrationPlan> all = loadAll();
        all.removeIf(p -> Objects.equals(p.getPlanId(), id));
        saveAll(all);
    }
}
