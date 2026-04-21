package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import java.util.List;
import java.util.Optional;

public interface AdminRepositoryPort {
    Optional<Admin> findByUserEID(String userEID);
    Optional<Admin> findById(Integer id);
    boolean existsById(Integer id);
    Admin save(Admin admin);
    void deleteById(Integer id);
    List<Admin> findAll();
    long count();
}
