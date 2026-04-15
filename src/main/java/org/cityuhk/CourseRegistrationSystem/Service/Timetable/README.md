# Timetable Service Refactoring Documentation

## Overview

The timetable export functionality has been refactored from a monolithic approach in `RegistrationService` into a well-structured, extensible service architecture following SOLID principles and GoF design patterns.

## Architecture Changes

### Before: Monolithic Design
```
RegistrationService.ExportTimeTable()
├── Student validation
├── Fetch registration records
├── Sort records
├── Format text output
├── Write to file
└── Return file path
```

### After: Layered, Modular Design
```
TimetableService (Facade)
├── Internal validation helpers
│   └── Validates student and timetable data
├── TimetableData (DTO with Builder)
│   └── Immutable data holder
└── TimetableExporter (Strategy Pattern)
    ├── TimetableFormatter
    │   └── Formats rows/headers
    └── Concrete Exporters
        └── TextTimetableExporter
```

---

## SOLID Principles Applied

### **S - Single Responsibility Principle**

Each class has a single, well-defined responsibility:

- **TimetableService**: Orchestrates timetable export workflow and enforces export-specific validation
- **TimetableData**: Holds timetable information
- **TimetableFormatter**: Formats timetable output
- **TextTimetableExporter**: Handles text file export logic

**Benefit**: Changes to validation logic don't affect export logic, and vice versa. Each class is easier to test and understand.

### **O - Open/Closed Principle**

The design is open for extension but closed for modification:

- **TimetableExporter** interface allows new export formats (CSV, JSON, PDF) without modifying existing code
- New formatters can be added by implementing `TimetableFormatter`
- New validators can be added by extending validation logic

**Example**: To add CSV export, simply implement `TimetableExporter` without touching existing classes:
```java
@Component
public class CsvTimetableExporter implements TimetableExporter {
    // CSV export implementation
}
```

### **L - Liskov Substitution Principle**

All `TimetableExporter` implementations are interchangeable:

```java
// Can use any exporter that implements TimetableExporter
public Path exportTimetable(Integer studentId, TimetableExporter exporter) {
    // Works with TextTimetableExporter, CsvTimetableExporter, etc.
}
```

### **I - Interface Segregation Principle**

Small, focused interfaces instead of large monolithic ones:

- **TimetableExporter**: Only export-related methods
- **TimetableFormatter**: Only formatting methods
- **TimetableService** private helpers: Only timetable validation methods

**Benefit**: Clients depend only on methods they actually use.

### **D - Dependency Inversion Principle**

High-level modules depend on abstractions, not concrete implementations:

```java
public class TimetableService {
    private final TimetableExporter defaultExporter; // Depends on abstraction
    
    public Path exportTimetable(Integer studentId, TimetableExporter exporter) {
        // Can use any implementation
    }
}
```

---

## GoF Design Patterns Applied

### **1. Strategy Pattern** (TimetableExporter)

**Problem**: Need to support multiple export formats (TXT, CSV, PDF, JSON).

**Solution**: Define a common interface for different export strategies.

```java
public interface TimetableExporter {
    Path export(TimetableData timetableData);
    String getFileExtension();
    String getFormatName();
}
```

**Implementations**:
- `TextTimetableExporter` - Text format
- `CsvTimetableExporter` - CSV format (future)
- `PdfTimetableExporter` - PDF format (future)

**Benefit**: At runtime, select the appropriate exporter without tight coupling.

### **2. Builder Pattern** (TimetableData)

**Problem**: TimetableData has multiple optional parameters and complex construction logic.

**Solution**: Use a Builder to provide flexible, readable object construction.

```java
TimetableData timetableData = new TimetableData.Builder()
    .studentId(123)
    .registrationRecords(records)
    .dayFormatter(DateTimeFormatter.ofPattern("EEEE"))
    .timeFormatter(DateTimeFormatter.ofPattern("HH:mm"))
    .build();
```

**Benefits**:
- Clear, readable construction
- Immutable objects after creation
- Easy to add optional parameters
- Validation during build

### **3. Facade Pattern** (TimetableService)

**Problem**: Export workflow has multiple complex steps (validation, building, formatting, exporting).

**Solution**: Provide a single, simplified interface that coordinates these steps.

```java
public class TimetableService {
    public Path exportTimetable(Integer studentId) {
        // Facade hides complexity:
        // 1. Validate student
        // 2. Build timetable data
        // 3. Validate data
        // 4. Export using exporter
    }
}
```

**Benefits**:
- Clients don't need to know implementation details
- Easy to change workflow without affecting clients
- Coordinates complex operations

### **4. Decorator Pattern** (Implicit in Formatter)

**TimetableFormatter** acts as a decorator adding formatting capabilities:

```java
public interface TimetableFormatter {
    String formatTitle(Integer studentId);
    String formatHeader();
    String formatRow(RegistrationRecord record);
}
```

**Benefit**: Can chain formatters or add formatting logic without modifying export logic.

---

## New Classes

### Core Components

| Class | Purpose | Pattern |
|-------|---------|---------|
| `TimetableService` | Orchestrates timetable export | Facade |
| `TimetableData` | Timetable information holder | Builder, DTO |
| `TimetableExporter` | Export strategy interface | Strategy |
| `TextTimetableExporter` | Text file exporter | Strategy implementation |
| `TimetableFormatter` | Format rows/headers interface | Strategy |
| `TextTimetableFormatter` | Text format implementation | Strategy implementation |
| `TimetableExportException` | Export-specific exception | Custom Exception |
| `TimetableValidationException` | Validation-specific exception | Custom Exception |

### Benefits of New Architecture

1. **Extensibility**: Add new export formats by implementing `TimetableExporter`
2. **Maintainability**: Each class has a single, clear responsibility
3. **Testability**: Each component can be tested in isolation
4. **Reusability**: `TimetableData` can be used by multiple exporters
5. **Flexibility**: Support different formatters via dependency injection
6. **Error Handling**: Specific exceptions for different failure modes

---

## Usage Examples

### Basic Export (Default Text Format)
```java
@Autowired
private TimetableService timetableService;

public void exportStudentTimetable(Integer studentId) {
    try {
        Path file = timetableService.exportTimetable(studentId);
        // Use file...
    } catch (TimetableValidationException e) {
        // Handle validation error
    } catch (TimetableExportException e) {
        // Handle export error
    }
}
```

### Export with Custom Exporter
```java
@Autowired
private TimetableService timetableService;

@Autowired
private TimetableExporter csvExporter; // Could be CsvTimetableExporter

public void exportAsCSV(Integer studentId) {
    try {
        Path file = timetableService.exportTimetable(studentId, csvExporter);
        // Use file...
    } catch (TimetableValidationException | TimetableExportException e) {
        // Handle error
    }
}
```

### Get Timetable Data Without Exporting
```java
@Autowired
private TimetableService timetableService;

public void checkTimetable(Integer studentId) {
    try {
        TimetableData data = timetableService.getTimetableData(studentId);
        System.out.println("Student " + data.getStudentId() + 
                         " has " + data.getRegistrationRecords().size() + 
                         " registered courses");
    } catch (TimetableValidationException e) {
        // Handle error
    }
}
```

### Future: Add CSV Export
```java
@Component
public class CsvTimetableExporter implements TimetableExporter {
    private final TimetableFormatter csvFormatter;
    
    @Override
    public Path export(TimetableData timetableData) throws TimetableExportException {
        // CSV export implementation
    }
    
    @Override
    public String getFileExtension() {
        return ".csv";
    }
    
    @Override
    public String getFormatName() {
        return "CSV";
    }
}
```

---

## Migration Summary

### Updated Classes
- **RegistrationService**: Removed `ExportTimeTable()` method (responsibility moved to TimetableService)
- **RegistrationController**: Updated to inject and use `TimetableService` instead of calling `RegistrationService.ExportTimeTable()`

### Backward Compatibility
- The export endpoint `/api/registration/export-timetable` remains unchanged
- HTTP contract is identical
- Error handling is improved with specific exception types

### Breaking Changes
- None for external API consumers
- Internal: `RegistrationService.ExportTimeTable()` no longer exists (internal only)

---

## Testing Strategy

Each component can be tested independently:

```java
// Test TimetableService validation
@Test
public void testValidateStudentNotFound() {
    assertThrows(TimetableValidationException.class, 
    () -> timetableService.exportTimetable(invalidId));
}

// Test TimetableFormatter
@Test
public void testFormatRow() {
    String row = formatter.formatRow(registrationRecord);
    assertNotNull(row);
    assertTrue(row.contains("MON")); // Day
}

// Test TimetableExporter
@Test
public void testExport() throws TimetableExportException {
    Path file = exporter.export(timetableData);
    assertTrue(Files.exists(file));
}

// Test TimetableService (Integration)
@Test
public void testExportTimetable() throws Exception {
    Path file = timetableService.exportTimetable(studentId);
    assertTrue(Files.exists(file));
}
```

---

## Performance Considerations

1. **Memory**: TimetableData is immutable; no copying overhead
2. **Disk**: Temporary files are created in system temp directory (efficient)
3. **Database**: Single query per student for registration records (no N+1)
4. **Formatting**: Lazy formatting during export (no pre-computation)

---

## Future Enhancements

1. **Additional Exporters**:
   - CSV format with configurable delimiter
   - JSON format for API clients
   - PDF format with styling
   - iCalendar format for calendar apps

2. **Filtering**:
   - Filter by semester
   - Filter by course type (LECTURE, TUTORIAL, LAB)
   - Filter by day of week

3. **Caching**:
   - Cache timetable data for repeated exports
   - Cache formatter instances

4. **Validation Rules**:
   - Add warnings for timetable conflicts
   - Add validation for credit limits
   - Add validation for prerequisites

5. **User Preferences**:
   - Store user-preferred export format
   - Store user-preferred formatters
   - Store custom sorting preferences

---

## Conclusion

This refactoring significantly improves code quality through:
- ✅ **SOLID principles** - maintainable, extensible code
- ✅ **GoF patterns** - proven design solutions
- ✅ **Separation of concerns** - each class has one responsibility
- ✅ **Dependency injection** - loose coupling, high testability
- ✅ **Extensibility** - easily add new export formats
- ✅ **Better error handling** - specific exception types
- ✅ **Immutability** - safer data handling
- ✅ **Clean API** - simple facade for complex operations
