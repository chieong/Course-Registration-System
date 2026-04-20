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

- `list-courses`
- `add-section <sectionId>`
- `drop-section <sectionId>`
- `export-timetable [outputPath]`

## Admin Commands

- `admin-list-users`
- `admin-create-user <userEID> <name> <password>`
- `admin-modify-user <staffId> <userEID> <name> [password]`
- `admin-remove-user <staffId>`
- `admin-create-course --code <code> --title <title> --credits <credits> [--term <term>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]`
- `admin-modify-course --code <code> [--title <title>] [--credits <credits>] [--term <term>] [--description <desc>] [--prereq <A,B>] [--exclusive <X,Y>]`
- `admin-remove-course <courseCode>`

## Notes

- Use double quotes for arguments with spaces.
- Some commands require a logged-in user with the correct role.
