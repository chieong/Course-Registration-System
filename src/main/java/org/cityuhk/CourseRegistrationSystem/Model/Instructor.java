package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instructor")
public class Instructor extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffId;

    private String department;

    protected Instructor() {
        // Required by JPA for entity instantiation.
    }

    public Instructor(InstructorBuilder builder) {
        super(builder);
        this.staffId = builder.staffId;
        this.department = builder.department;
    }

    public static class InstructorBuilder extends User.Builder<InstructorBuilder> {
        private Integer staffId;
        private String department;

        public InstructorBuilder withStaffId(Integer staffId) {
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
        public Instructor build() {
            return new Instructor(this);
        }
    }

    public Integer getStaffId() {
        return this.staffId;
    }

    public String getDepartment() {
        return this.department;
    }
}

