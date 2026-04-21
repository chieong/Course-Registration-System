package org.cityuhk.CourseRegistrationSystem.Repository;

import org.cityuhk.CourseRegistrationSystem.Model.WaitlistRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WaitlistRecordRepository extends JpaRepository<WaitlistRecord,Integer> {
    @Query("select count(w) from WaitlistRecord w where w.section.sectionId = :sectionId")
    int countWaitlisted(@Param("sectionId") Integer sectionId);

    @Query("select case when count(w) > 0 then true else false end from WaitlistRecord w " +
            "where w.student.studentId = :studentId and w.section.sectionId = :sectionId")
    boolean exists(@Param("studentId") Integer studentId, @Param("sectionId") Integer sectionId);
}
