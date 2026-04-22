package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import org.cityuhk.CourseRegistrationSystem.Model.Course;
import org.cityuhk.CourseRegistrationSystem.Model.Section;

import java.time.LocalDateTime;

public class AdminSectionService {
    private Integer sectionId;
    private Course course;
    private Integer enrollCapacity;
    private Integer waitlistCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Section.Type sectionType;
    private String venue;

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

    // Getter and Setter for enrollCapacity
    public Integer getEnrollCapacity() {
        return enrollCapacity;
    }

    public void setEnrollCapacity(int enrollCapacity) {
        this.enrollCapacity = enrollCapacity;
    }

    // Getter and Setter for waitlistCapacity
    public Integer getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public void setWaitlistCapacity(int waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

    // Getter and Setter for startTime
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    // Getter and Setter for endTime
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // Getter and Setter for sectionType
    public Section.Type getSectionType() {
        return sectionType;
    }

    public void setSectionType(Section.Type sectionType) {
        this.sectionType = sectionType;
    }

    // Getter and Setter for venue
    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

}
