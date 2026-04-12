package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;

import org.cityuhk.CourseRegistrationSystem.Model.Course;

public interface ICourseRepository {

	/**
	 * 
	 * @param courseId
	 */
	Course getCourseById(String courseId);

	ArrayList<Course> getAllCourse();

	/**
	 * 
	 * @param course
	 */
	void createCourse(Course course);

	/**
	 * 
	 * @param courseId
	 * @param updatedCourse
	 */
	void updateCourse(String courseId, Course updatedCourse);

	/**
	 * 
	 * @param courseId
	 */
	void removerCourse(String courseId);

}