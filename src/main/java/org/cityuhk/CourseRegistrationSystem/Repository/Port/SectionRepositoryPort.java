package org.cityuhk.CourseRegistrationSystem.Repository.Port;

import org.cityuhk.CourseRegistrationSystem.Model.Section;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SectionRepositoryPort {
    Optional<Section> findById(Integer id);
    Section save(Section section);
    void deleteById(Integer id);
    List<Section> findAll();
    boolean overlapsInVenue(String venue, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);
}
