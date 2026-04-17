package org.cityuhk.CourseRegistrationSystem.RestController;

import org.cityuhk.CourseRegistrationSystem.Service.Section.SectionService;
import org.cityuhk.CourseRegistrationSystem.Model.Section;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class ViewMasterClassScheduleRestController {
    private final SectionService sectionService;

    @Autowired
    public ViewMasterClassScheduleRestController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public ResponseEntity<List<Section>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }


}

