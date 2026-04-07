package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
public class Course { //for creating a course

	private String courseCode;
	private String title;
	private int credits;
	private String description;
	private ArrayList<String> ExclusiveCourseCode;
	private ArrayList<String> PrerequisiteCourses;
	private ArrayList<Section> sections;
	private String term;

	public Course(String courseCode,String title,int credits,String description,String term){ 
		this.courseCode=courseCode;
		this.title=title;
		this.credits=credits;
		this.description=description;
		this.term=term;
		this.ExclusiveCourseCode = new ArrayList<>();
		this.PrerequisiteCourses = new ArrayList<>();
		this.sections = new ArrayList<>();
	}

	



}