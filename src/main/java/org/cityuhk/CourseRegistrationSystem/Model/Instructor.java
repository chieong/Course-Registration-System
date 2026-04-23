package org.cityuhk.CourseRegistrationSystem.Model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

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

    @ManyToMany(mappedBy = "instructors")
    private Set<Section> sections = new HashSet<>();

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

    public Set<Section> getSections() {
        return sections;
    }

    public void setSections(Set<Section> sections) {
        this.sections = sections != null ? sections : new HashSet<>();
    }

    // Changing the return type from Set<RegistrationRecord> to Set<Section>
    // since instructors teach sections, they don't have registration records.
    public Set<Section> getTimeTable() {
        return this.sections; // Removed UnsupportedOperationException
    }

    public void addSection(Section section) {
        this.sections.add(section);
    }

    public void removeSection(Section section) {
        this.sections.remove(section);
    }
}

