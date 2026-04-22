package org.cityuhk.CourseRegistrationSystem.Cli;

final class CliSession {
    private final String userEid;
    private final CliRole role;

    CliSession(String userEid, CliRole role) {
        this.userEid = userEid;
        this.role = role;
    }

    String getUserEid() {
        return userEid;
    }

    CliRole getRole() {
        return role;
    }
}
