# Java Syntax and Symbols Guide (Based on Your Code)

This guide explains the common symbols and syntax used in your project.

## 1) Package and Import

- `package org.cityuhk.CourseRegistrationSystem;`
  - Declares which namespace this file belongs to.

- `import x.y.z.ClassName;`
  - Lets you use a class without writing the full path every time.

- `import static x.y.z.ClassName.methodName;`
  - Imports static methods directly, so you can write `assertThrows(...)` instead of `Assertions.assertThrows(...)`.

## 2) Class and Method Declarations

- `public class RegistrationServiceTest { ... }`
  - `public`: accessible from anywhere.
  - `class`: blueprint for objects.

- `public void deleteStudent(Integer id) { ... }`
  - `void`: method returns no value.
  - `Integer id`: input parameter and type.

## 3) Object Creation and Assignment

- `Type name = value;`
  - Declares variable type and assigns a value.

- `new Type(...)`
  - Creates a new object.

Example:
- `RegistrationService service = new RegistrationService(studentRepo, sectionRepo, recordRepo);`

## 4) Generics: `<T>`

- `Optional<Student>`
  - `Optional` is a container that may contain `Student` or be empty.

- `Optional<T> findById(ID id)`
  - Generic repository method signature.
  - Means: find by id, return present/empty safely.

## 5) Optional Syntax

- `Optional.empty()`
  - No value exists.

- `Optional.of(student)`
  - Wraps a non-null value.

- `existingStudent.isPresent()`
  - Checks if value exists.

- `existingStudent.get()`
  - Extracts the value (only safe after checking presence).

## 6) Conditionals and Operators

- `if (condition) { ... }`
  - Executes block only if condition is true.

- `!condition`
  - Logical NOT (negation).

Example:
- `if (!existingStudent.isPresent()) { ... }`
  - If student is NOT present, run this block.

## 7) Exceptions

- `throw new RuntimeException("Student not found");`
  - Immediately stops normal flow and throws an error.

## 8) Annotations (`@...`)

- `@Service`
  - Marks a Spring service bean.

- `@Autowired`
  - Tells Spring to inject dependencies.

- `@Transactional`
  - Runs method in a DB transaction.

- `@Test`
  - Marks a JUnit test method.

## 9) Lambda Expression (`->`)

Example:
- `() -> service.addSection(...)`

Meaning:
- A small anonymous function with no input parameters.
- Used by `assertThrows` to run code that is expected to fail.

## 10) Mockito Test Syntax

- `mock(Type.class)`
  - Creates a fake object.

- `when(mock.method(...)).thenReturn(value)`
  - Stub behavior: when called, return this value.

- `verify(mock).method(...)`
  - Assert that method was called.

- `any(RegistrationRecord.class)`
  - Matcher meaning: any object of this class is acceptable.

## 11) Common Symbols Quick Reference

- `.` : access method/field (`object.method()`)
- `()` : method call or parameter list
- `{}` : code block scope
- `;` : end of statement
- `,` : separate arguments
- `"..."` : string literal
- `< >` : generic type parameters
- `=` : assignment
- `==` : value comparison (for primitives)
- `!` : logical NOT
- `->` : lambda
- `@` : annotation marker

## 12) Your Key Line Explained

`registrationRecordRepository.save(student.addSection(section, timestamp, enrolled, semester));`

Execution order:
1. `student.addSection(...)` runs first and returns a `RegistrationRecord`.
2. That returned record is passed into `registrationRecordRepository.save(...)`.
3. `save(...)` writes it to database.

So this is nested method call syntax: outer call uses inner call result.

## 13) How to Read Any Java Line (Simple Formula)

Read from left to right using this template:
- Who is doing it? (object/class)
- What action? (method)
- With what inputs? (arguments inside `(...)`)
- What comes back? (return type/value)

