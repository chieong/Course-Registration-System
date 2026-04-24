# CLI Quick Reference

## Start CLI

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--cli"
```

## Session Commands

- `help`
- `login <userEID> <password>`
- `logout`
- `whoami`
- `exit` or `quit`

A local session file is saved at:

- `%USERPROFILE%\.course-registration-cli-session.properties`

## Student Commands

### Registration
- `list-courses`
- `view-master-schedule`
- `add-section <sectionId>`
- `drop-section <sectionId>`
- `join-waitlist <sectionId>`
- `drop-waitlist <sectionId>`
- `export-timetable [outputPath]`

### Plan Management
- `list-plans`
- `create-plan [priority]`
- `remove-plan <planId>`
- `add-plan-entry <planId> <sectionId> <SELECTED|WAITLIST> [joinWaitlistOnAddFailure]`
- `remove-plan-entry <planId> <entryId>`
- `reorder-plans <planIdCsv>`

### Timetable
- `show-timetable`
- `export-timetable <outputPath>`

## Admin Commands

### Admin Users
- `admin-list-users`
- `admin-create-user <userEID> <name> <password>`
- `admin-modify-user <staffId> <userEID> <name> [password]`
- `admin-remove-user <staffId>`

### Student Users
- `admin-list-students`
- `admin-create-student <userEID> <name> <password> [--major <major>] [--dept <dept>]`
- `admin-modify-student <studentId> <userEID> <name> [password] [--major <major>] [--dept <dept>]`
- `admin-remove-student <studentId>`

### Instructor Users
- `admin-list-instructors`
- `admin-create-instructor <userEID> <name> <password> [--dept <dept>]`
- `admin-modify-instructor <staffId> <userEID> <name> [password] [--dept <dept>]`
- `admin-remove-instructor <staffId>`

### Courses
- `admin-create-course --code <code> [--title <title>] [--credits <credits>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]` (creates if missing, updates if exists)
- `admin-modify-course --code <code> [--title <title>] [--credits <credits>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]` (alias of `admin-create-course`)
- `admin-remove-course <courseCode>`

### Sections
- `admin-list-sections [--course <courseCode>]`
- `admin-create-section --course <courseCode> --type <LECTURE|TUTORIAL|LAB> --enroll-capacity <int> --waitlist-capacity <int> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm> --venue <venue> [--instructors <idCsv>]`
- `admin-modify-section --section-id <id> [--course <courseCode>] [--type <LECTURE|TUTORIAL|LAB>] [--enroll-capacity <int>] [--waitlist-capacity <int>] [--start <yyyy-MM-ddTHH:mm>] [--end <yyyy-MM-ddTHH:mm>] [--venue <venue>] [--instructors <idCsv>]`
- `admin-remove-section <sectionId>`

### Registration Periods
- `admin-list-periods [--cohort <cohort>]`
- `admin-create-period --cohort <cohort> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>`
- `admin-delete-period <periodId>`

## Notes

- Use double quotes for arguments with spaces.
- Some commands require a logged-in user with the correct role.
