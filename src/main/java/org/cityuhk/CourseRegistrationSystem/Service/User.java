package org.cityuhk.CourseRegistrationSystem.Service;
public abstract class User {

	private String UserEID;
	private String name;

	public User(Builder<?> builder) {
		this.UserEID = builder.userEID;
		this.name = builder.name;
	}

	public abstract static class Builder<T extends Builder<T>> {
		private String userEID;
		private String name;

		public T withUserEID(String userEID) {
			this.userEID = userEID;
			return self();
		}

		public T withName(String name) {
			this.name = name;
			return self();
		}

		protected abstract T self();

		public abstract User build();
	}
	public String getUserEID() {
		return UserEID;
	}

	public String getUserName() {
		return name;
	}

}