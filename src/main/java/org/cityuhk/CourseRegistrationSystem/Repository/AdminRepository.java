package org.cityuhk.CourseRegistrationSystem.Repository;

import java.util.Optional;

import org.cityuhk.CourseRegistrationSystem.Model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
	Optional<Admin> findByUserEID(String userEID);
}