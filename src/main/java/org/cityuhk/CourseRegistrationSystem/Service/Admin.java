package org.cityuhk.CourseRegistrationSystem.Service;
public class Admin extends User implements IStaff {

	private int staffId;

	public Admin(AdminBuilder builder) {
		super(builder);
		this.staffId = builder.staffId;
	}
	public static class AdminBuilder extends User.Builder<AdminBuilder> {
		private int staffId;

		public AdminBuilder withStaffId(int staffId) {
			this.staffId = staffId;
			return self();
		}

		@Override
		protected AdminBuilder self() {
			return this;
		}

		@Override
		public User build() {
			return new Admin(this);
		}
	}
	public int getStaffId() {
		return this.staffId;
	}

}