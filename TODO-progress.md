# Course Registration Progress TODO

Use this checklist to track each use case in two steps:
- Step 1: Frontend request path exists on website
- Step 2: Backend function works and enforces authorization

## Priority 1: Login and Access Control

### Use Case: Login

#### Step 1 (Frontend)
- [/] Login page is reachable from navigation or direct URL (`/login`)
- [/] Login form has username and password fields
- [/] Login form submits to a real backend endpoint (currently form posts to `/login`)
- [/] Failed login shows error message on page
- [ ] Successful login redirects to a role-appropriate page
- [ ] Logout action is visible when user is logged in

#### Step 2 (Backend)
- [ ] Implement POST `/login` authentication handler
- [ ] Validate username/password against real user data source
- [ ] Store authenticated user in session/security context
- [ ] Add logout endpoint and clear session/security context
- [ ] Return login error on invalid credentials
- [ ] Add automated tests for successful and failed login

### Use Case: Role-Based Access Control

#### Step 1 (Frontend)
- [ ] Hide protected menu links for guests (not logged in)
- [ ] Show only allowed links by role (Student/Teacher/Admin)
- [ ] Hide "View Student List" for Student role
- [ ] Redirect unauthorized users to login or access denied page
- [ ] Add clear access denied message page

#### Step 2 (Backend)
- [ ] Define roles and permissions matrix
- [ ] Protect all pages: deny access when not logged in
- [ ] Enforce: Student cannot access `/studentlist`
- [ ] Enforce access rules for `/manageplan`, `/timetable`, `/ViewMasterClassSchedule`
- [ ] Return 403 for authenticated but unauthorized access
- [ ] Add integration tests for guest/student/teacher/admin access

## Priority 2: Existing Website Use Cases

### Use Case: View Master Class Schedule (`/ViewMasterClassSchedule`)

#### Step 1 (Frontend)
- [ ] Navigation link exists and works
- [ ] Page has clear entry point and user flow

#### Step 2 (Backend)
- [ ] Endpoint returns real schedule data (not hardcoded placeholders)
- [ ] Apply role check for allowed roles
- [ ] Add endpoint/service tests

### Use Case: View Timetable (`/timetable`)

#### Step 1 (Frontend)
- [ ] Navigation link exists and works
- [ ] User can request timetable for current term

#### Step 2 (Backend)
- [ ] Endpoint loads timetable from backend data source
- [ ] Restrict access to logged-in users only
- [ ] Add tests for allowed/denied access and data retrieval

### Use Case: Manage Plan (`/manageplan`)

#### Step 1 (Frontend)
- [ ] Navigation link exists and works
- [ ] User can submit plan/filter actions from page controls

#### Step 2 (Backend)
- [ ] Handle plan update/add/drop actions via backend endpoints
- [ ] Validate business rules (credit limits, conflicts, prerequisites)
- [ ] Restrict to allowed roles
- [ ] Add tests for success, validation failure, and unauthorized access

### Use Case: View Student List (`/studentlist`)

#### Step 1 (Frontend)
- [ ] Navigation link is hidden for Student role
- [ ] Page request path available for authorized roles only

#### Step 2 (Backend)
- [ ] Enforce authorization: block Student role
- [ ] Return real student list data per course
- [ ] Add tests for teacher/admin allowed and student denied

## Quick Status Board

- [ ] Step 1 complete for all use cases
- [ ] Step 2 complete for all use cases
- [ ] Login + role checks complete (minimum milestone)
