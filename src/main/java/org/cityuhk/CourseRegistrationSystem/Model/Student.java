package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Student extends User
// implements IAcademic, IStudent
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentId;

    private int minSemesterCredit;
    private int maxSemesterCredit;
    private String major;
    private int cohort;
    private String department;
    private int maxDegreeCredit; // this is the total credit for each student for their degree

    // Stores the student's registered classes (their timetable), not implemented yet
    // @ManyToMany private Set<Course> completedCourses = new HashSet<>();
    // @ManyToMany private Set<Section> enrolledSections = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "student_completed_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> completedCourses = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<RegistrationRecord> registrationRecords = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<RegistrationPlan> registrationPlans = new HashSet<>();

    protected Student() {
        // Required by JPA for entity instantiation.
    }

    public Student(StudentBuilder builder) {
        super(builder);
        this.studentId = builder.studentId;
        this.minSemesterCredit = builder.minSemesterCredit;
        this.maxSemesterCredit = builder.maxSemesterCredit;
        this.major = builder.major;
        this.cohort = builder.cohort;
        this.department = builder.department;
        this.maxDegreeCredit = builder.maxDegreeCredit;
        // this.completedCourses = builder.completedCourses;
        // this.enrolledSections = builder.enrolledSections;
    }

    public static class StudentBuilder extends User.Builder<StudentBuilder> {
        private Integer studentId;
        private int minSemesterCredit;
        private int maxSemesterCredit;
        private String major;
        private int cohort;
        private String department;
        private int maxDegreeCredit;

        // private Set<Course> completedCourses = new HashSet<>();
        // private Set<Section> enrolledSections = new HashSet<>();

        public StudentBuilder withStudentId(Integer studentId) {
            this.studentId = studentId;
            return self();
        }

        public StudentBuilder withMinSemesterCredit(int minSemesterCredit) {
            this.minSemesterCredit = minSemesterCredit;
            return self();
        }

        public StudentBuilder withMaxSemesterCredit(int maxSemesterCredit) {
            this.maxSemesterCredit = maxSemesterCredit;
            return self();
        }

        public StudentBuilder withMajor(String major) {
            this.major = major;
            return self();
        }

        public StudentBuilder withCohort(int cohort) {
            this.cohort = cohort;
            return self();
        }

        public StudentBuilder withDepartment(String department) {
            this.department = department;
            return self();
        }

        public StudentBuilder withMaxDegreeCredit(int maxDegreeCredit) {
            this.maxDegreeCredit = maxDegreeCredit;
            return self();
        }

        // public StudentBuilder withEnrolledCourses(Set<Course> completedCourses) {
        //     this.completedCourses = completedCourses;
        //     return self();
        // }
        //
        // public StudentBuilder withEnrolledSections(Set<Section> enrolledSections) {
        //     this.enrolledSections = enrolledSections;
        //     return self();
        // }

        @Override
        protected StudentBuilder self() {
            return this;
        }

        @Override
        public Student build() {
            return new Student(this);
        }
    }

    public boolean validateSemesterCreditCount(int additional) {

        int sum = additional;
        for (RegistrationRecord record : registrationRecords) {
            sum = record.addCredits(sum);
        }

        return (sum >= minSemesterCredit && sum <= maxSemesterCredit);
    }

    public String getDepartment() {
        return department;
    }

    public boolean satisfyPrerequisites(Course course) {
        return course.satisfyPrerequisites(completedCourses);
    }

    public boolean notTakenExclusives(Course course) {
        return course.notTakenExclusives(completedCourses);
    }

    public boolean hasCredits(int additional) {
        return (additional) > maxSemesterCredit;
    }

    public RegistrationRecord dropSection(Section section, LocalDateTime timestamp) {
        return new RegistrationRecord(this, section, timestamp);
    }

    public Integer getStudentId() {
        return studentId;
    }

    public int getMinSemesterCredit() {
        return minSemesterCredit;
    }

    public int getMaxSemesterCredit() {
        return maxSemesterCredit;
    }

    public String getMajor() {
        return major;
    }

    public int getMaxDegreeCredit() {
        return maxDegreeCredit;
    }

    public Integer getCohort() {
        return cohort;
    }

    public Set<RegistrationPlan> getRegistrationPlans() {
        return registrationPlans;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(super.toString());
        result.append(String.format("Student Id: %d\n",studentId));
        result.append(String.format("Min Semester Credit: %d\n",minSemesterCredit));
        result.append(String.format("Max Semester Credit: %d\n",maxSemesterCredit));
        result.append(String.format("Major: %s\n",major));
        result.append(String.format("Cohort: %s\n",cohort));
        result.append(String.format("Department: %s\n",department));
        result.append(String.format("Max Degree Credit: %d\n",maxDegreeCredit));
        return result.toString();
    }
}
