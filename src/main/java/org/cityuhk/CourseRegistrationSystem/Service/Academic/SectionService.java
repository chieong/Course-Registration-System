package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.CourseRepository;
import org.cityuhk.CourseRegistrationSystem.Repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService {

    public final SectionRepository sectionRepository;
    public final CourseRepository courseRepository;

    @Autowired
    public SectionService(SectionRepository sectionRepository, CourseRepository courseRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }
}
