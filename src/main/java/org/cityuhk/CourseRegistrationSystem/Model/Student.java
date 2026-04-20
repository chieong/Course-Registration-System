package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
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
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> completedCourses = new HashSet<>();

    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<RegistrationRecord> registrationRecords = new HashSet<>();



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
        private int studentId;
        private int minSemesterCredit;
        private int maxSemesterCredit;
        private String major;
        private int cohort;
        private String department;
        private int maxDegreeCredit;
        // private Set<Course> completedCourses = new HashSet<>();
        // private Set<Section> enrolledSections = new HashSet<>();

        public StudentBuilder withStudentId(int studentId) {
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

    // @Override
    public boolean validateSemesterCreditCount(int additional) {

        int sum = additional;
        for (RegistrationRecord record : registrationRecords) {
            sum = record.addCredits(sum);
        }

        return (sum >= minSemesterCredit && sum <= maxSemesterCredit);
    }

    // @Override
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

    public RegistrationRecord addSection(Section section, LocalDateTime timestamp, int enrolled) {
        if (!section.canEnroll(this, enrolled)) {
            throw new RuntimeException();
        }


        return new RegistrationRecord(this, section, timestamp);
    }

    public RegistrationRecord dropSection(Section section, LocalDateTime timestamp) {
        return new RegistrationRecord(this, section, timestamp);
    }
    // getter
    // add when needed
    public Integer getStudentId() {
        return studentId;
    }
    public Integer getCohort() {
        return cohort;
    }
    
}

