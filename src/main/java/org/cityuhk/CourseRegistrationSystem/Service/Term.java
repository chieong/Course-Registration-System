package org.cityuhk.CourseRegistrationSystem.Service;

public class Term {

    private final int year;
    private final String semester;

    public Term(int year, String semester) {
        this.year = year;
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public String getSemester() {
        return semester;
    }
}
