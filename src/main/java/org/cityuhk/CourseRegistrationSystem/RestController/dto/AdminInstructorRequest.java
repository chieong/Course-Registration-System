package org.cityuhk.CourseRegistrationSystem.RestController.dto;

public class AdminInstructorRequest {
    private String userEID;
    private String name;
    private Integer staffId;
    private String password;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }
}
