package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Set;

public class Instructor extends User
// implements IStaff, IAcademic
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffId;

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
        return staffId;
    }

    public String getDepartment() {
        return this.department;
    }

    public Set<RegistrationRecord> getTimeTable() {
        // TODO - implement Instructor.getTimeTable
        throw new UnsupportedOperationException();
    }
}
