package org.cityuhk.CourseRegistrationSystem.Service;

public class RegistrationOperation {

    private String courseCode;
    private int sectionId;

    public RegistrationOperation() {
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
}
