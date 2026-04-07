package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
public class RegistrationPlan {
	//stores a list of RegistrationRecord for each plan of a student
	
	//Plan: Before reg period, a plan is stored for executing after reg period starts.
	//	to execute the RegistrationRecord action (add,drop) each by each

	private int planId;
	private String createdAt;
	private int studentId;
	private ArrayList<RegistrationRecord> RecordOperations;




}