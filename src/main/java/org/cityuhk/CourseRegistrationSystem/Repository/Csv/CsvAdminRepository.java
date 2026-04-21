package org.cityuhk.CourseRegistrationSystem.Repository.Csv;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.AdminRepositoryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@ConditionalOnProperty(name = "app.persistence.type", havingValue = "csv")
public class CsvAdminRepository implements AdminRepositoryPort {

    static final String FILE = "admins.csv";
    static final String[] HEADER = {"staffId", "userEID", "name", "password"};

    private final CsvFileStore store;
    private final CsvIdGenerator idGen;

    public CsvAdminRepository(CsvFileStore store, CsvIdGenerator idGen) {
        this.store = store;
        this.idGen = idGen;
    }

    private List<Admin> loadAll() {
        List<Admin> admins = new ArrayList<>();
        for (String[] row : store.readRows(FILE)) {
            if (row.length < 4) continue;
            try {
                int staffId = Integer.parseInt(row[0]);
                Admin admin = new Admin.AdminBuilder()
                        .withStaffId(staffId)
                        .withUserEID(row[1])
                        .withName(row[2])
                        .withPassword(row[3])
                        .build();
                admins.add(admin);
            } catch (NumberFormatException ignored) {
            }
        }
        return admins;
    }

    private void saveAll(List<Admin> admins) {
        List<String[]> rows = admins.stream()
                .map(a -> new String[]{
                        String.valueOf(a.getStaffId()),
                        safe(a.getUserEID()),
                        safe(a.getUserName()),
                        safe(a.getPassword())
                })
                .collect(Collectors.toList());
        store.writeRows(FILE, HEADER, rows);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public Optional<Admin> findByUserEID(String userEID) {
        return loadAll().stream()
                .filter(a -> a.getUserEID() != null && a.getUserEID().equalsIgnoreCase(userEID))
                .findFirst();
    }

    @Override
    public Optional<Admin> findById(Integer id) {
        return loadAll().stream()
                .filter(a -> a.getStaffId() == id)
                .findFirst();
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    @Override
    public synchronized Admin save(Admin admin) {
        List<Admin> all = loadAll();
        if (admin.getStaffId() == 0) {
            // new entity – generate an ID via reflection workaround: rebuild with new ID
            int newId = idGen.nextId("admin");
            admin = new Admin.AdminBuilder()
                    .withStaffId(newId)
                    .withUserEID(admin.getUserEID())
                    .withName(admin.getUserName())
                    .withPassword(admin.getPassword())
                    .build();
            all.add(admin);
        } else {
            int id = admin.getStaffId();
            all.removeIf(a -> a.getStaffId() == id);
            all.add(admin);
        }
        saveAll(all);
        return admin;
    }

    @Override
    public synchronized void deleteById(Integer id) {
        List<Admin> all = loadAll();
        all.removeIf(a -> a.getStaffId() == id);
        saveAll(all);
    }

    @Override
    public List<Admin> findAll() {
        return loadAll();
    }

    @Override
    public long count() {
        return loadAll().size();
    }
}
