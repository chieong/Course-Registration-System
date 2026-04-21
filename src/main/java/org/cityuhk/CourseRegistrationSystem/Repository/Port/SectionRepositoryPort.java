package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import java.util.List;
import java.util.Optional;

public interface SectionRepositoryPort {
    Optional<Section> findById(Integer id);
    Section save(Section section);
    void deleteById(Integer id);
    List<Section> findAll();
}
