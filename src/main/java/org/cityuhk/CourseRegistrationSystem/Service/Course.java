package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;

public class Course { // for creating a course

	private String courseCode;
	private String title;
	private int credits;
	private String description;
	private ArrayList<String> ExclusiveCourseCode;
	private ArrayList<String> PrerequisiteCourses;
	private ArrayList<Section> sections;
	private String term;

	public Course(String courseCode, String title, int credits, String description, String term,
			ArrayList<String> ExclusiveCourseCode, ArrayList<String> PrerequisiteCourses, ArrayList<Section> sections) {
		this.courseCode = courseCode;
		this.title = title;
		this.credits = credits;
		this.description = description;
		this.term = term;
		this.ExclusiveCourseCode = ExclusiveCourseCode;
		this.PrerequisiteCourses = PrerequisiteCourses;
		this.sections = sections;
	}

	
	// getter
	public int getCredits() {

		return credits;
	}

	public Section SearchSectionID(int sectionId) {
		for (Section section : sections) {
			if (section.getSectionID() == sectionId) {
				return section;
			}
		}
		return null;
	}

}