package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;

import java.time.LocalDateTime;
import java.util.Set;

public class AdminSectionService {
    private Integer sectionId;
    private Course course;
    private Integer enrollCapacity;
    private Integer waitlistCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Section.Type sectionType;
    private String venue;
    private Set<Integer> instructorStaffIds;

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Integer getEnrollCapacity() {
        return enrollCapacity;
    }

    public void setEnrollCapacity(int enrollCapacity) {
        this.enrollCapacity = enrollCapacity;
    }

    public Integer getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public void setWaitlistCapacity(int waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Section.Type getSectionType() {
        return sectionType;
    }

    public void setSectionType(Section.Type sectionType) {
        this.sectionType = sectionType;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Set<Integer> getInstructorStaffIds() {
        return instructorStaffIds;
    }

    public void setInstructorStaffIds(Set<Integer> instructorStaffIds) {
        this.instructorStaffIds = instructorStaffIds;
    }
}
