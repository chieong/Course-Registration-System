package org.cityuhk.CourseRegistrationSystem.Service.Academic;

import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.CourseRepositoryPort;
import org.cityuhk.CourseRegistrationSystem.Repository.Port.SectionRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionService {

    public final SectionRepositoryPort sectionRepository;
    public final CourseRepositoryPort courseRepository;

    @Autowired
    public SectionService(SectionRepositoryPort sectionRepository, CourseRepositoryPort courseRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
    }

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }
}
