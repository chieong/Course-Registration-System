# 1. Introduction

This document provides a guide for using the Command Line Interface (CLI) of the Course Registration System. 
The CLI allows users (Students, Instructors, and Admins) to interact with the system through text-based commands, including:

- Course registration
- Timetable management
- Plan management
- Administrative operations

# 2. Getting Started

## Running the System in CLI Mode
### Run the following command in your terminal:
```powershell
java -jar .\course-registration-system-0.0.1-SNAPSHOT.jar
```
## Default Login Credentials:
| Role            | EID             | Password        |
| --------------- | --------------- | --------------- |
| Admin           | admin           | admin123        |
| Student         | student1        | student123      |
| Instructor      | instructor1     | instructor123   |


## General Session Commands

- `help` : list all executatble commands
- `login <userEID> <password>` : login with EID and password
- `logout` logout current account
- `whoami`: list your current role
- `exit` or `quit` : terminate program

## Session Storage
### A local session file is stored at:

- `%USERPROFILE%\.course-registration-cli-session.properties`

## Student Commands

### Registration
- `list-courses` 
- `view-master-schedule` : show all avaliable course and sections
- `add-section <sectionId>` : add a specific section to student's timetable, sectionId can be seen in master class schedule
- `drop-section <sectionId>` 
- `join-waitlist <sectionId>`
- `export-timetable [outputPath]`
- `show-timetable`

### Plan Management
### Explaination: When its not in registration period, student can create plans for autosubmittion when registration period started, each plan stored a list of section and course registered by student
- `list-plans`
- `create-plan [priority]` priority : 1 is the highest priority
- `remove-plan <planId>`
- `add-plan-entry <planId> <sectionId> <SELECTED|WAITLIST> [joinWaitlistOnAddFailure]` joinWaitlistOnAddFailure : true/false. If set to true and section is full, joins waitlist instead
- `remove-plan-entry <planId> <entryId>` entryId : the ID of a specific section inside a plan. E.g. 1,101
- `reorder-plans <planIdCsv>` planIdCsv : comma-separated list of plan IDs (no spaces). E.g. `reorder-plans 3,1,2` : re-arrange plan order-1st plan becomes 3rd, and so on

## Admin Commands

### Admin Users
- `admin-list-users`
- `admin-create-user <userEID> <name> <password>` 
- `admin-modify-user <userEID> <name> [password]` 
- `admin-remove-user <userEID>`

### Student Users
- `admin-list-students`
- `admin-create-student <userEID> <name> <password> <minSemesterCredit> <maxSemesterCredit> <major> <cohort> <department> <maxDegreeCredit> [<completedCourseCode1,completedCourseCode2,...,completedCourseCodeN>]`
- `admin-modify-student <userEID> [--name <name>] [--password <password>] [--min-creds <minSemesterCredit>] [--max-creds <maxSemesterCredit>] [--major <major>] [--cohort <cohort>] [--dept <dept>] [--max-degree <maxDegreeCredit>]` 
- `admin-remove-student <userEID>`

### Instructor Users
### [--dept] : optional, meaning: department belongs to 
- `admin-list-instructors`
- `admin-create-instructor <userEID> <name> <password> [--dept <dept>]`
- `admin-modify-instructor <userEID> <name> [password] [--dept <dept>]`
- `admin-remove-instructor <userEID>`

### Courses
- `admin-create-course --code <code> --title <title> --credits <credits> [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>] (creates if missing, updates if exists)` (creates if missing, updates if exists), 
- `admin-modify-course --code <code> [--title <title>] [--credits <credits>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]` (alias of `admin-create-course`)
- `admin-remove-course <courseCode>`
Example: 
```
admin-create-course --code CS3342 --title Software Design --credit 3 --description This is the software course --prereq CS123.CS1234 --exclusive CS2144,CS53
```

Note:
[--prereq <A,B>] : require student to have the course registered before.  <br>
[--exclusive <X,Y>] : course that require student never registered before  <br>
### Sections
### section is the detailed course info which tells the type of the course "LECTURE|TUTORIAL|LAB"
- `admin-create-section --course <courseCode> --type <LECTURE|TUTORIAL|LAB> --enroll-capacity <int> --waitlist-capacity <int> --weekday <int [1,7]> --start <HH:mm> --end <HH:mm> --venue <venue> [--instructors <idCsv>]`
- `admin-modify-section --section-id <id> [--course <courseCode>] [--type <LECTURE|TUTORIAL|LAB>] [--enroll-capacity <int>] [--waitlist-capacity <int>] [--weekday <int [1,7]>] [--start <HH:mm>] [--end <HH:mm>] [--venue <venue>] [--instructors <idCsv>]`   
- `admin-remove-section <sectionId>`

Example:
```
admin-modify-section --section-id 12 --course CS3342 --type LECTURE --enroll-capacity 80 --waitlist-capacity 20 --start 2026-05-01T09:00 --end 2026-05-01T10:50 --venue "Y4701" --instructors 1,2
```
### Registration Periods
- `admin-list-periods [--cohort <cohort>]`
- `admin-create-period --cohort <cohort> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>`
- `admin-delete-period <periodId>`

## Notes

- `<required field>` The field is required
- `[optional field]` The field is optional
- `yyyy-MM-ddTHH:mm`: 'T' is a literal, i.e. 2026-04-04T23:59
- Use double quotes for arguments with spaces.
- Some commands require a logged-in user with the correct role.

