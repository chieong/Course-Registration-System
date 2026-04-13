package org.cityuhk.CourseRegistrationSystem.Service;

import org.cityuhk.CourseRegistrationSystem.Model.AcademicRecord;
import org.springframework.core.io.ByteArrayResource;

import tools.jackson.databind.ser.jdk.JDKMiscSerializers.ByteArrayOutputStreamSerializer;
import org.cityuhk.CourseRegistrationSystem.Model.User;

public class AcademicService {

    private final IAcademicRecordRepository academicRecordRepository;
    private final IUserRepository userRepository;

    public AcademicService(IAcademicRecordRepository academicRecordRepository, IUserRepository userRepository) {
        this.academicRecordRepository = academicRecordRepository;
        this.userRepository = userRepository;
    }

    public ByteArrayResource getTimeTable(String userEID) {
        User user = userRepository.getUser(userEID);
        if (user == null || !(user instanceof IAcademic)) {
            throw new RuntimeException("User not found or not an academic");
        }
        return new ByteArrayResource(getTimetable((IAcademic) user).getBytes());
    }

    public String getTimetable (IAcademic academic) {
        return "Bruh";
    }
}