package org.cityuhk.CourseRegistrationSystem.Service;

import java.util.ArrayList;
public class Instructor extends User implements IStaff, IAcademic {

	private int staffId;
	private String department;

	public Instructor(InstructorBuilder builder) {
		super(builder);
		this.staffId = builder.staffId;
		this.department = builder.department;
	}

	public static class InstructorBuilder extends User.Builder<InstructorBuilder> {
		private int staffId;
		private String department;

		public InstructorBuilder withStaffId(int staffId) {
			this.staffId = staffId;
			return self();
		}

		public InstructorBuilder withDepartment(String department) {
			this.department = department;
			return self();
		}

		@Override
		protected InstructorBuilder self() {
			return this;
		}

		@Override
		public User build() {
			return new Instructor(this);
		}
	}

	public int getStaffId() {
		// TODO - implement Instructor.getStaffId
		throw new UnsupportedOperationException();
	}

	public String getDepartment() {
		return this.department;
	}

	public ArrayList<RegistrationRecord> getTimeTable() {
		// TODO - implement Instructor.getTimeTable
		throw new UnsupportedOperationException();
	}

}