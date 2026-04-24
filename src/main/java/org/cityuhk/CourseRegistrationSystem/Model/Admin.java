package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Admin extends User
// implements IStaff
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffId;

    protected Admin() {
        // Required by JPA for entity instantiation.
    }

    public Admin(AdminBuilder builder) {
        super(builder);
        this.staffId = builder.staffId;
    }

    public static class AdminBuilder extends User.Builder<AdminBuilder> {
        private Integer staffId;

        public AdminBuilder withStaffId(Integer staffId) {
            this.staffId = staffId;
            return self();
        }

        @Override
        protected AdminBuilder self() {
            return this;
        }

        @Override
        public Admin build() {
            return new Admin(this);
        }
    }

    public Integer getStaffId() {
        return this.staffId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("Staff ID: ").append(this.staffId).append("\n");
        return sb.toString();
    }
}

