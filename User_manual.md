# 1. Introduction

## This document provides a guide for using the Command Line Interface (CLI) of the Course Registration System. <br> <br>The CLI allows users (Students, Instructors, and Admins) to interact with the system through text-based commands, including:

- Course registration
- Timetable management
- Plan management
- Administrative operations

# 2. Getting Started

## Running the System in CLI Mode
### Run the following command in your terminal:
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--cli"
```
## Default Login Credentials:
```role based access
Admin: EID: admin , Password: admin123
Student: EID: student1 , Password: student123 
Instructor: EID: instructor1 , Password: instructor123
```

## General Session Commands

- `help` : list all executatble commands
- `login <userEID> <password>` 
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
- `add-section <sectionId>` : add a specific section to student's timetable <sectionId> : (e.g: 1,2,11,101) 
- `drop-section <sectionId>` 
- `join-waitlist <sectionId>`
- `export-timetable [outputPath]` : output path is optional
- `show-timetable`

### Plan Management
### Explaination: When its not in registration period, student can create plans for autosubmittion when registration period started, each plan stored a list of section and course registered by student
- `list-plans`
- `create-plan [priority]` [priority] : optional, 1 is the highest priority
- `remove-plan <planId>`
- `add-plan-entry <planId> <sectionId> <SELECTED|WAITLIST> [joinWaitlistOnAddFailure] `  [joinWaitlistOnAddFailure] : optional, field: true/false. select true to enable system add your section to waitlist if it is full
- `remove-plan-entry <planId> <entryId>` : <entryId> is the ID of a specific section inside a plan. E.g. 1,101
- `reorder-plans <planIdCsv>` : <planIdCsv> = comma-separated list of plan IDs (no spaces). E.g. `reorder-plans 3,1,2` : re-arrange plan order-1st plan becomes 3rd, and so on

## Admin Commands

### Admin Users
- `admin-list-users` : list all admin role user
- `admin-create-user <userEID> <name> <password>` 
- `admin-modify-user <userEID> <name> [password]`  <br>: [password] is optional
- `admin-remove-user <userEID>` : [staffId] is the Id stored in the database, for example <admin-remove-user 1> 

### Student Users
- `admin-list-students` list all student role user
- `admin-create-student <userEID> <name> <password> <minSemesterCredit> <maxSemesterCredit> <major> <cohort> <department> <maxDegreeCredit> [<completedCourseCode1,completedCourseCode2,...,completedCourseCodeN>]`
- `admin-modify-student <userEID> [--name <name>] [--password <password>] [--min-creds <minSemesterCredit>] [--max-creds <maxSemesterCredit>] [--major <major>] [--cohort <cohort>] [--dept <dept>] [--max-degree <maxDegreeCredit>]`  <br>: [ ] fields are optional
- `admin-remove-student <userEID>` : [userEID] E.g: khplee3, admin1

### Instructor Users
### [--dept] : optional, meaning: department belongs to 
- `admin-list-instructors`
- `admin-create-instructor <userEID> <name> <password> [--dept <dept>]`
- `admin-modify-instructor <userEID> <name> [password] [--dept <dept>]`
- `admin-remove-instructor <userEID>`

### Courses
'[ ]' fields are optional,  <br>
[--prereq <A,B>] : require student to have the course registered before.  <br>
[--exclusive <X,Y>] : course that require student never registered before  <br>
- `admin-create-course --code <code> --title <title> --credits <credits> [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>] (creates if missing, updates if exists)` (creates if missing, updates if exists), 
- `admin-modify-course --code <code> [--title <title>] [--credits <credits>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]` (alias of `admin-create-course`)
- `admin-remove-course <courseCode>` <br>
Example: `admin-create-course --code CS3342 --title Software Design --credit 3 --description This is the software course --prereq CS123.CS1234 --exclusive CS2144,CS53`

### Sections
### Notes: courseCode is optional, <yyyy-MM-ddTHH:mm>: 'T' needs to fill in  <br> E.g: 2026-04-04T23:59
### section is the detailed course info which tells the type of the course "LECTURE|TUTORIAL|LAB"
- `admin-create-section --course <courseCode> --type <LECTURE|TUTORIAL|LAB> --enroll-capacity <int> --waitlist-capacity <int> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm> --venue <venue> [--instructors <idCsv>]`
- `admin-modify-section --section-id <id> [--course <courseCode>] [--type <LECTURE|TUTORIAL|LAB>] [--enroll-capacity <int>] [--waitlist-capacity <int>] [--start <yyyy-MM-ddTHH:mm>] [--end <yyyy-MM-ddTHH:mm>] [--venue <venue>] [--instructors <idCsv>]`   
- `admin-remove-section <sectionId>`  <br>you may use `admin-list-section` to find the exact section id <br>
Example: <br>
`admin-modify-section --section-id 12 --course CS3342 --type LECTURE --enroll-capacity 80 --waitlist-capacity 20 --start 2026-05-01T09:00 --end 2026-05-01T10:50 --venue "Y4701" --instructors 1,2` 
### Registration Periods
- `admin-list-periods [--cohort <cohort>]`
- `admin-create-period --cohort <cohort> --start <yyyy-MM-ddTHH:mm> --end <yyyy-MM-ddTHH:mm>`
- `admin-delete-period <periodId>`

## Notes

- Use double quotes for arguments with spaces.
- Some commands require a logged-in user with the correct role.

