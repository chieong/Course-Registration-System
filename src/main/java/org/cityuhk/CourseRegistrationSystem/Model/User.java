package org.cityuhk.CourseRegistrationSystem.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class User {

    private String UserEID;
    private String name;
    private String password;

    protected User() {
        // Required by JPA for entity instantiation.
    }

    public User(Builder<?> builder) {
        this.UserEID = builder.userEID;
        this.name = builder.name;
        this.password = builder.password;
    }

    public abstract static class Builder<T extends Builder<T>> {
        private String userEID;
        private String name;
        private String password;

        public T withUserEID(String userEID) {
            this.userEID = userEID;
            return self();
        }

        public T withName(String name) {
            this.name = name;
            return self();
        }

        public T withPassword(String password) {
            this.password = password;
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

    @JsonIgnore
    public String getPassword() {
        return password;
    }
}

