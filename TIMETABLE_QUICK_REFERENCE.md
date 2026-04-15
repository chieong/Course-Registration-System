# Timetable Service Refactoring - Quick Reference

## What Changed?

### Removed
- `RegistrationService.ExportTimeTable()` method

### Added - New Package: `org.cityuhk.CourseRegistrationSystem.Service.Timetable`

| Class | Purpose |
|-------|---------|
| `TimetableService` | Orchestrates timetable export (Facade pattern) |
| `TimetableData` | Immutable DTO with Builder pattern support |
| `TimetableExporter` | Interface for export strategies |
| `TextTimetableExporter` | Text file export implementation |
| `TimetableFormatter` | Interface for formatting timetable rows |
| `TextTimetableFormatter` | Text format implementation |
| `TimetableValidator` | Validates student and timetable data |
| `TimetableExportException` | Custom exception for export failures |
| `TimetableValidationException` | Custom exception for validation failures |

### Updated
- `RegistrationController` - Uses `TimetableService` instead of `RegistrationService.ExportTimeTable()`
- `RegistrationService` - Timetable export logic removed, remains focused on registration

---

## How to Use

### Basic Export
```java
@Autowired
private TimetableService timetableService;

public void exportTimetable(Integer studentId) {
    try {
        Path file = timetableService.exportTimetable(studentId);
        // Use file...
    } catch (TimetableValidationException e) {
        // Handle validation error (400 Bad Request)
    } catch (TimetableExportException e) {
        // Handle export error (500 Internal Server Error)
    }
}
```

### With Custom Exporter (Future)
```java
path = timetableService.exportTimetable(studentId, csvExporter);
```

### Get Timetable Data Only
```java
TimetableData data = timetableService.getTimetableData(studentId);
System.out.println("Records: " + data.getRegistrationRecords().size());
```

---

## SOLID Principles Applied

| Principle | Implementation |
|-----------|-----------------|
| **S**ingle Responsibility | Each class has one purpose |
| **O**pen/Closed | Can add new exporters without modifying existing code |
| **L**iskov Substitution | All TimetableExporter implementations are interchangeable |
| **I**nterface Segregation | Small, focused interfaces |
| **D**ependency Inversion | Depends on TimetableExporter abstraction |

---

## GoF Patterns Applied

| Pattern | Class | Benefit |
|---------|-------|---------|
| **Strategy** | TimetableExporter + implementations | Easy to add new export formats |
| **Builder** | TimetableData.Builder | Flexible, readable object construction |
| **Facade** | TimetableService | Hides complexity, single entry point |
| **Decorator** | TimetableFormatter | Adds formatting capabilities independently |

---

## Testing

### Unit Tests Location
`src/test/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/TimetableServiceTests.java`

### Test Categories
- ✅ Formatter tests - Verify text formatting works correctly
- ✅ Builder tests - Verify TimetableData construction and validation
- ✅ Exporter tests - Verify export strategy pattern
- ✅ Exception tests - Verify error handling

### Strategy Pattern Tests
- Tests show how different exporters can be used interchangeably
- Foundation for adding CSV, PDF, JSON exporters

---

## Endpoint Contract (Unchanged)

```http
GET /api/registration/export-timetable?studentId={studentId}
Authorization: Bearer <admin_token>
Content-Type: text/plain

Response:
- 200 OK: Timetable file as attachment
- 400 Bad Request: Validation error (student not found, no records)
- 500 Internal Server Error: Export failure
```

---

## Future Enhancements (Easy to Implement)

### 1. CSV Export
```java
@Component
public class CsvTimetableExporter implements TimetableExporter {
    // Just implement the interface
}
```

### 2. PDF Export
```java
@Component
public class PdfTimetableExporter implements TimetableExporter {
    // Just implement the interface
}
```

### 3. Custom Formatters
```java
@Component
public class CompactTimetableFormatter implements TimetableFormatter {
    // Different formatting style
}
```

---

## Key Files

### Documentation
- [Timetable Service README](src/main/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/README.md) - Detailed architecture guide
- [Refactoring Summary](TIMETABLE_REFACTORING_SUMMARY.md) - Before/after comparison

### Implementation
- [TimetableService.java](src/main/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/TimetableService.java) - Main service
- [TextTimetableExporter.java](src/main/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/TextTimetableExporter.java) - Export strategy
- [TimetableData.java](src/main/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/TimetableData.java) - Builder DTO

### Tests
- [TimetableServiceTests.java](src/test/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/TimetableServiceTests.java) - Unit tests

---

## Build Status

✅ **Compiles Successfully** - No errors or warnings  
✅ **Tests Pass** - All unit tests pass  
✅ **Backward Compatible** - Public API endpoints unchanged  

---

## Questions?

Refer to [README.md](src/main/java/org/cityuhk/CourseRegistrationSystem/Service/Timetable/README.md) for detailed documentation on architecture, patterns, and usage examples.
