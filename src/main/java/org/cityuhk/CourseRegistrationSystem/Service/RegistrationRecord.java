package org.cityuhk.CourseRegistrationSystem.Service;
public class RegistrationRecord {

	private Student student;
	private Section section;
	private int timestamp;
	public RegistrationState state;

	public RegistrationRecord(Student student, Section section, int timestamp) {
		this.student = student;
		this.section = section;
		this.timestamp = timestamp;
		this.state = new PendingState(); // Initialize with a default state
	}

	public void setState(RegistrationState state) {
		this.state = state;
	}

	public Section getSection() {
		return this.section;
	}

	public Student getStudent() {
		return this.student;
	}

	public void confirm() {
		this.state.confirm(this);
	}

	public void drop() {
		this.state.drop(this);
	}

	public void waitlist() {
		this.state.waitlist(this);
	}

	public String getStatus() {
		return this.state.getStatusName();
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
}