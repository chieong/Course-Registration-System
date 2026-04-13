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
    private int staffId;

    protected Admin() {
        // Required by JPA for entity instantiation.
    }

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

