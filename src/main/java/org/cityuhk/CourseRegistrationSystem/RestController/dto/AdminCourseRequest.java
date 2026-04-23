package org.cityuhk.CourseRegistrationSystem.RestController.dto;

import org.cityuhk.CourseRegistrationSystem.Model.Section;

import java.util.Set;

public class AdminCourseRequest {
    private String courseCode;
    private String title;
    private int credits;
    private String description;
    private Set<String> prerequisiteCourseCodes;
    private Set<String> exclusiveCourseCodes;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getPrerequisiteCourseCodes() {
        return prerequisiteCourseCodes;
    }

    public void setPrerequisiteCourseCodes(Set<String> prerequisiteCourseCodes) {
        this.prerequisiteCourseCodes = prerequisiteCourseCodes;
    }

    public Set<String> getExclusiveCourseCodes() {
        return exclusiveCourseCodes;
    }

    public void setExclusiveCourseCodes(Set<String> exclusiveCourseCodes) {
        this.exclusiveCourseCodes = exclusiveCourseCodes;
    }
}
