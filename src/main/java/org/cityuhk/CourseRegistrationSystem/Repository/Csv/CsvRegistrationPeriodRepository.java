package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cityuhk.CourseRegistrationSystem.Model.RegistrationPeriod;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.RegistrationPeriodRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvRegistrationPeriodRepository implements RegistrationPeriodRepositoryPort {

    static final String FILE = "registration_periods.csv";
    static final String[] HEADER = {"periodId", "cohort", "startDateTime", "endDateTime"};

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;

    public CsvRegistrationPeriodRepository(CsvFileStore store, CsvIdGenerator idGen) {
        this.store = store;
        this.idGen = idGen;
    }

    private List<RegistrationPeriod> loadAll() {
        List<RegistrationPeriod> periods = new ArrayList<>();
        for (String[] row : store.readRows(FILE)) {
            if (row.length < 4) continue;
            try {
                RegistrationPeriod rp = new RegistrationPeriod();
                rp.setPeriodId(Integer.parseInt(row[0]));
                rp.setCohort(Integer.parseInt(row[1]));
                rp.setStartDateTime(row[2].isBlank() ? null : LocalDateTime.parse(row[2]));
                rp.setEndDateTime(row[3].isBlank() ? null : LocalDateTime.parse(row[3]));
                periods.add(rp);
            } catch (Exception ignored) {
            }
        }
        return periods;
    }

    private synchronized void saveAll(List<RegistrationPeriod> periods) {
        List<String[]> rows = periods.stream().map(p -> new String[]{
                String.valueOf(p.getPeriodId()),
                String.valueOf(p.getCohort()),
                p.getStartDateTime() == null ? "" : p.getStartDateTime().toString(),
                p.getEndDateTime() == null ? "" : p.getEndDateTime().toString()
        }).collect(Collectors.toList());
        store.writeRows(FILE, HEADER, rows);
    }

    private static String safe(String v) { return v == null ? "" : v; }

    @Override
    public Optional<RegistrationPeriod> findById(Integer id) {
        return loadAll().stream()
                .filter(p -> Objects.equals(p.getPeriodId(), id))
                .findFirst();
    }

    @Override
    public List<RegistrationPeriod> findAll() {
        return loadAll();
    }

    @Override
    public synchronized RegistrationPeriod save(RegistrationPeriod period) {
        List<RegistrationPeriod> all = loadAll();
        if (period.getPeriodId() == null) {
            period.setPeriodId(idGen.nextId("registration_period"));
            all.add(period);
        } else {
            Integer id = period.getPeriodId();
            all.removeIf(p -> Objects.equals(p.getPeriodId(), id));
            all.add(period);
        }
        saveAll(all);
        return period;
    }

    @Override
    public Optional<RegistrationPeriod> findActivePeriod(int cohort, LocalDateTime now) {
        return loadAll().stream()
                .filter(p -> p.getCohort() == cohort)
                .filter(p -> p.getStartDateTime() != null && p.getEndDateTime() != null)
                .filter(p -> !now.isBefore(p.getStartDateTime()) && !now.isAfter(p.getEndDateTime()))
                .findFirst();
    }
}
