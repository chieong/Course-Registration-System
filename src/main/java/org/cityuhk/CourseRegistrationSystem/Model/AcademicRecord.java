package org.cityuhk.CourseRegistrationSystem.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    // private int completedCredit;
    private int completedCredit;
    private float GPA;
    private String standing;
    @OneToMany 
    public AcademicRecord() {}

    public AcademicRecord(int recordId, int completedCredit, float GPA, String standing) {
        this.recordId = recordId;
        this.completedCredit = completedCredit;
        this.GPA = GPA;
        this.standing = standing;
    }
}

