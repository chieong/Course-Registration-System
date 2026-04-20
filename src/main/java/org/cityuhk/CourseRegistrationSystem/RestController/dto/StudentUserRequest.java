package org.cityuhk.CourseRegistrationSystem.RestController.dto;

public class StudentUserRequest {
    private String userEID;
    private String name;
    private String password;
    private String major;
    private String department;

    public String getUserEID() {
        return userEID;
    }

    public void setUserEID(String userEID) {
        this.userEID = userEID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
