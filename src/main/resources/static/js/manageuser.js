(function () {
  const state = {
    users: [],
    editingKey: null,
    deletingKey: null,
    notificationTimer: null,
    notificationHideTimer: null,
    initialized: false
  };

  const els = {
    tableBody: document.getElementById("muTableBody"),
    statTotal: document.getElementById("muStatTotal"),
    statActive: document.getElementById("muStatActive"),
    statStudent: document.getElementById("muStatStudent"),
    statStaff: document.getElementById("muStatStaff"),
    search: document.getElementById("muSearch"),
    roleFilter: document.getElementById("muRoleFilter"),
    statusFilter: document.getElementById("muStatusFilter"),
    addBtn: document.getElementById("muAddBtn"),
    dialog: document.getElementById("muDialog"),
    deleteDialog: document.getElementById("muDeleteDialog"),
    dialogTitle: document.getElementById("muDialogTitle"),
    closeDialogBtn: document.getElementById("muCloseDialogBtn"),
    cancelDialogBtn: document.getElementById("muCancelDialogBtn"),
    saveBtn: document.getElementById("muSaveBtn"),
    form: document.getElementById("muForm"),
    formError: document.getElementById("muFormError"),
    username: document.getElementById("muUsername"),
    fullName: document.getElementById("muFullName"),
    email: document.getElementById("muEmail"),
    role: document.getElementById("muRole"),
    status: document.getElementById("muStatus"),
    password: document.getElementById("muPassword"),
    studentFields: document.getElementById("muStudentFields"),
    instructorFields: document.getElementById("muInstructorFields"),
    major: document.getElementById("muMajor"),
    department: document.getElementById("muDepartment"),
    cohort: document.getElementById("muCohort"),
    minCredit: document.getElementById("muMinCredit"),
    maxCredit: document.getElementById("muMaxCredit"),
    maxDegreeCredit: document.getElementById("muMaxDegreeCredit"),
    instructorDepartment: document.getElementById("muInstructorDepartment"),
    deleteDetails: document.getElementById("muDeleteDetails"),
    closeDeleteBtn: document.getElementById("muCloseDeleteBtn"),
    cancelDeleteBtn: document.getElementById("muCancelDeleteBtn"),
    confirmDeleteBtn: document.getElementById("muConfirmDeleteBtn"),
    notification: document.getElementById("muNotification"),
    notificationText: document.getElementById("muNotificationText"),
    notificationClose: document.getElementById("muNotificationClose")
  };

  const requiredElementKeys = [
    "tableBody",
    "statTotal",
    "statActive",
    "statStudent",
    "statStaff",
    "search",
    "roleFilter",
    "statusFilter",
    "addBtn",
    "dialog",
    "deleteDialog",
    "dialogTitle",
    "closeDialogBtn",
    "cancelDialogBtn",
    "saveBtn",
    "form",
    "formError",
    "username",
    "fullName",
    "email",
    "role",
    "status",
    "password",
    "studentFields",
    "instructorFields",
    "major",
    "department",
    "cohort",
    "minCredit",
    "maxCredit",
    "maxDegreeCredit",
    "instructorDepartment",
    "deleteDetails",
    "closeDeleteBtn",
    "cancelDeleteBtn",
    "confirmDeleteBtn",
    "notification",
    "notificationText",
    "notificationClose"
  ];

  function hasAllRequiredElements() {
    return requiredElementKeys.every(function (key) {
      return !!els[key];
    });
  }

  function makeUserKey(role, id) {
    return role + ":" + String(id);
  }

  function parseUserKey(key) {
    const parts = String(key || "").split(":");
    return {
      role: parts[0] || "",
      id: Number(parts[1])
    };
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function normalize(value) {
    return String(value || "").toLowerCase().trim();
  }

  async function apiRequest(url, options) {
    const response = await fetch(url, Object.assign({
      headers: {
        "Content-Type": "application/json"
      }
    }, options || {}));

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "Request failed.");
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  }

  function mapAdminToUi(admin) {
    return {
      key: makeUserKey("Admin", admin.staffId),
      id: admin.staffId,
      username: admin.userEID || "",
      fullName: admin.userName || "",
      email: admin.userEID ? (admin.userEID + "@cityu.local") : "",
      role: "Admin",
      status: "Active",
      department: "",
      major: "",
      cohort: "",
      minSemesterCredit: "",
      maxSemesterCredit: "",
      maxDegreeCredit: ""
    };
  }

  function mapStudentToUi(student) {
    return {
      key: makeUserKey("Student", student.studentId),
      id: student.studentId,
      username: student.userEID || "",
      fullName: student.name || "",
      email: student.userEID ? (student.userEID + "@cityu.local") : "",
      role: "Student",
      status: "Active",
      department: student.department || "",
      major: student.major || "",
      cohort: student.cohort || "",
      minSemesterCredit: student.minSemesterCredit || "",
      maxSemesterCredit: student.maxSemesterCredit || "",
      maxDegreeCredit: student.maxDegreeCredit || ""
    };
  }

  function mapInstructorToUi(instructor) {
    return {
      key: makeUserKey("Instructor", instructor.staffId),
      id: instructor.staffId,
      username: instructor.userEID || "",
      fullName: instructor.name || "",
      email: instructor.userEID ? (instructor.userEID + "@cityu.local") : "",
      role: "Instructor",
      status: "Active",
      department: instructor.department || "",
      major: "",
      cohort: "",
      minSemesterCredit: "",
      maxSemesterCredit: "",
      maxDegreeCredit: ""
    };
  }

  async function loadUsers() {
    const responses = await Promise.all([
      apiRequest("/api/admin/users", { method: "GET" }),
      apiRequest("/api/admin/students", { method: "GET" }),
      apiRequest("/api/admin/instructors", { method: "GET" })
    ]);

    const admins = Array.isArray(responses[0]) ? responses[0].map(mapAdminToUi) : [];
    const students = Array.isArray(responses[1]) ? responses[1].map(mapStudentToUi) : [];
    const instructors = Array.isArray(responses[2]) ? responses[2].map(mapInstructorToUi) : [];

    state.users = admins.concat(students, instructors);
    renderAll();
  }

  function getFilteredUsers() {
    const search = normalize(els.search.value);
    const roleFilter = els.roleFilter.value;
    const statusFilter = els.statusFilter.value;

    return state.users.filter(function (user) {
      const matchesSearch = !search || [user.username, user.fullName, user.email]
        .some(function (field) { return normalize(field).includes(search); });
      const matchesRole = !roleFilter || user.role === roleFilter;
      const matchesStatus = !statusFilter || user.status === statusFilter;
      return matchesSearch && matchesRole && matchesStatus;
    });
  }

  function renderStats() {
    const total = state.users.length;
    const active = state.users.filter(function (u) { return u.status === "Active"; }).length;
    const students = state.users.filter(function (u) { return u.role === "Student"; }).length;
    const staff = state.users.filter(function (u) { return u.role === "Admin" || u.role === "Instructor"; }).length;

    els.statTotal.textContent = String(total);
    els.statActive.textContent = String(active);
    els.statStudent.textContent = String(students);
    els.statStaff.textContent = String(staff);
  }

  function renderTable() {
    const rows = getFilteredUsers();

    if (!rows.length) {
      els.tableBody.innerHTML = "<tr class=\"empty-row\"><td colspan=\"6\">No users found for the selected filters.</td></tr>";
      return;
    }

    els.tableBody.innerHTML = rows.map(function (user) {
      return "\n        <tr>\n          <td>" + escapeHtml(user.username) + "</td>\n          <td>" + escapeHtml(user.fullName) + "</td>\n          <td>" + escapeHtml(user.email) + "</td>\n          <td>" + escapeHtml(user.role) + "</td>\n          <td><span class=\"mu-status-active\">" + escapeHtml(user.status) + "</span></td>\n          <td>\n            <div class=\"inline-actions\">\n              <button type=\"button\" class=\"small-btn\" data-action=\"edit\" data-key=\"" + escapeHtml(user.key) + "\">Edit</button>\n              <button type=\"button\" class=\"small-danger\" data-action=\"delete\" data-key=\"" + escapeHtml(user.key) + "\">Delete</button>\n            </div>\n          </td>\n        </tr>\n      ";
    }).join("");
  }

  function renderAll() {
    renderStats();
    renderTable();
  }

  function hideNotification() {
    clearTimeout(state.notificationHideTimer);
    els.notification.classList.remove("is-visible");
    els.notification.classList.add("is-hiding");
    state.notificationHideTimer = setTimeout(function () {
      els.notification.classList.add("is-hidden");
      els.notification.classList.remove("is-hiding");
      els.notification.setAttribute("hidden", "hidden");
    }, 230);
  }

  function showNotification() {
    clearTimeout(state.notificationHideTimer);
    els.notification.removeAttribute("hidden");
    els.notification.classList.remove("is-hiding");
    els.notification.classList.remove("is-hidden");
    void els.notification.offsetWidth;
    els.notification.classList.add("is-visible");
  }

  function showMessage(message, type) {
    els.notificationText.textContent = message;
    els.notification.classList.remove("mu-notification-success", "mu-notification-info", "mu-notification-error");
    if (type === "error") {
      els.notification.classList.add("mu-notification-error");
    } else if (type === "info") {
      els.notification.classList.add("mu-notification-info");
    } else {
      els.notification.classList.add("mu-notification-success");
    }
    showNotification();
    clearTimeout(state.notificationTimer);
    state.notificationTimer = setTimeout(function () {
      hideNotification();
    }, 4000);
  }

  function showFormError(message) {
    els.formError.textContent = message;
    els.formError.classList.remove("is-hidden");
  }

  function clearFormError() {
    els.formError.textContent = "";
    els.formError.classList.add("is-hidden");
  }

  function showElement(element, show) {
    if (!element) {
      return;
    }
    if (show) {
      element.removeAttribute("hidden");
      element.classList.remove("is-hidden");
    } else {
      element.setAttribute("hidden", "hidden");
      element.classList.add("is-hidden");
    }
  }

  function toggleRoleFields(role) {
    showElement(els.studentFields, role === "Student");
    showElement(els.instructorFields, role === "Instructor");
  }

  function resetForm() {
    state.editingKey = null;
    els.dialogTitle.textContent = "Add User";
    els.saveBtn.textContent = "Add User";
    els.form.reset();
    els.status.value = "Active";
    els.role.value = "Admin";
    els.email.value = "";
    els.role.disabled = false;
    toggleRoleFields("Admin");
    els.username.readOnly = false;
    clearFormError();
  }

  function openModal(dialog) {
    if (dialog && typeof dialog.showModal === "function") {
      dialog.showModal();
      return;
    }
    if (dialog) {
      dialog.setAttribute("open", "open");
    }
  }

  function closeModal(dialog) {
    if (dialog && typeof dialog.close === "function") {
      dialog.close();
      return;
    }
    if (dialog) {
      dialog.removeAttribute("open");
    }
  }

  function openCreateDialog() {
    resetForm();
    openModal(els.dialog);
  }

  function openEditDialog(key) {
    const user = state.users.find(function (u) { return u.key === key; });
    if (!user) {
      return;
    }

    state.editingKey = key;
    els.dialogTitle.textContent = "Edit User";
    els.saveBtn.textContent = "Save Changes";
    els.username.value = user.username;
    els.fullName.value = user.fullName;
    els.email.value = user.email;
    els.role.value = user.role;
    els.status.value = "Active";
    els.password.value = "";
    els.major.value = user.major || "";
    els.department.value = user.department || "";
    els.cohort.value = user.cohort || "";
    els.minCredit.value = user.minSemesterCredit || "";
    els.maxCredit.value = user.maxSemesterCredit || "";
    els.maxDegreeCredit.value = user.maxDegreeCredit || "";
    els.instructorDepartment.value = user.department || "";
    toggleRoleFields(user.role);
    els.username.readOnly = false;
    els.role.disabled = true;
    clearFormError();
    openModal(els.dialog);
  }

  function closeDialog(options) {
    const notify = !!(options && options.notifyCancel);
    closeModal(els.dialog);
    els.role.disabled = false;
    if (notify) {
      showMessage(state.editingKey ? "User modification cancelled." : "User creation cancelled.", "info");
    }
  }

  function openDeleteDialog(key) {
    const user = state.users.find(function (u) { return u.key === key; });
    if (!user) {
      return;
    }

    state.deletingKey = key;
    els.deleteDetails.innerHTML = [
      ["Username", user.username],
      ["Full Name", user.fullName],
      ["Email", user.email],
      ["Role", user.role],
      ["Status", user.status]
    ].map(function (pair) {
      return "<div class=\"mu-detail-row\"><span class=\"mu-detail-label\">" + escapeHtml(pair[0]) + "</span><span>" + escapeHtml(pair[1]) + "</span></div>";
    }).join("");
    openModal(els.deleteDialog);
  }

  function closeDeleteDialog(options) {
    const notify = !!(options && options.notifyCancel);
    state.deletingKey = null;
    closeModal(els.deleteDialog);
    if (notify) {
      showMessage("User removal cancelled.", "info");
    }
  }

  function validateForm() {
    const username = els.username.value.trim();
    const fullName = els.fullName.value.trim();
    const role = els.role.value;
    const password = els.password.value;

    if (!username || !fullName || !role) {
      return "Please fill in all required fields.";
    }

    if (!state.editingKey && password.trim().length < 8) {
      return "Temporary password must be at least 8 characters for new users.";
    }

    if (role === "Student") {
      const minCredit = Number(els.minCredit.value || 0);
      const maxCredit = Number(els.maxCredit.value || 0);
      if (maxCredit < minCredit) {
        return "Max semester credits cannot be less than min semester credits.";
      }
    }

    return "";
  }

  function parseOptionalInteger(value) {
    const trimmed = String(value || "").trim();
    if (!trimmed) {
      return null;
    }
    const parsed = Number(trimmed);
    if (!Number.isFinite(parsed)) {
      return null;
    }
    return Math.trunc(parsed);
  }

  function getPayloadByRole(role) {
    if (role === "Student") {
      return {
        userEID: els.username.value.trim(),
        name: els.fullName.value.trim(),
        password: els.password.value || null,
        major: els.major.value.trim(),
        department: els.department.value.trim(),
        cohort: parseOptionalInteger(els.cohort.value),
        minSemesterCredit: parseOptionalInteger(els.minCredit.value),
        maxSemesterCredit: parseOptionalInteger(els.maxCredit.value),
        maxDegreeCredit: parseOptionalInteger(els.maxDegreeCredit.value)
      };
    }
    if (role === "Instructor") {
      return {
        userEID: els.username.value.trim(),
        name: els.fullName.value.trim(),
        password: els.password.value || null,
        department: els.instructorDepartment.value.trim()
      };
    }
    return {
      userEID: els.username.value.trim(),
      name: els.fullName.value.trim(),
      password: els.password.value || null
    };
  }

  function getEndpointByRole(role, isEdit, id) {
    if (role === "Student") {
      return isEdit ? "/api/admin/students/" + id : "/api/admin/students";
    }
    if (role === "Instructor") {
      return isEdit ? "/api/admin/instructors/" + id : "/api/admin/instructors";
    }
    return isEdit ? "/api/admin/users/" + id : "/api/admin/users";
  }

  async function saveUser(event) {
    event.preventDefault();
    clearFormError();

    const error = validateForm();
    if (error) {
      showFormError(error);
      return;
    }

    const isEdit = !!state.editingKey;
    const parsed = isEdit ? parseUserKey(state.editingKey) : null;
    const currentRole = parsed ? parsed.role : els.role.value;
    const currentId = parsed ? parsed.id : null;
    const confirmed = window.confirm(isEdit
      ? "Are you sure you want to save changes to this user account?"
      : "Are you sure you want to create this user account?");

    if (!confirmed) {
      showMessage(isEdit ? "User modification cancelled." : "User creation cancelled.", "info");
      return;
    }

    const payload = getPayloadByRole(currentRole);
    const endpoint = getEndpointByRole(currentRole, isEdit, currentId);

    try {
      await apiRequest(endpoint, {
        method: isEdit ? "PUT" : "POST",
        body: JSON.stringify(payload)
      });

      closeModal(els.dialog);
      els.role.disabled = false;
      await loadUsers();
      showMessage(isEdit ? "User account updated successfully." : "New user account created successfully.", "success");
    } catch (requestError) {
      showFormError(requestError.message || "Failed to save user.");
    }
  }

  async function confirmDelete() {
    if (!state.deletingKey) {
      return;
    }

    const user = state.users.find(function (u) { return u.key === state.deletingKey; });
    if (!user) {
      closeDeleteDialog();
      return;
    }

    const confirmed = window.confirm("Confirm removing user \"" + user.username + "\"?");
    if (!confirmed) {
      showMessage("User removal cancelled.", "info");
      return;
    }

    try {
      await apiRequest(getEndpointByRole(user.role, true, user.id), { method: "DELETE" });
      closeDeleteDialog();
      await loadUsers();
      showMessage("User account removed successfully.", "success");
    } catch (requestError) {
      showMessage(requestError.message || "Failed to remove user.", "error");
    }
  }

  function onTableClick(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) {
      return;
    }

    const key = button.getAttribute("data-key");
    if (!key) {
      return;
    }

    const action = button.getAttribute("data-action");
    if (action === "edit") {
      openEditDialog(key);
      return;
    }
    if (action === "delete") {
      openDeleteDialog(key);
    }
  }

  function bindEvents() {
    els.search.addEventListener("input", renderTable);
    els.roleFilter.addEventListener("change", renderTable);
    els.statusFilter.addEventListener("change", renderTable);

    els.addBtn.addEventListener("click", openCreateDialog);
    els.form.addEventListener("submit", saveUser);
    els.tableBody.addEventListener("click", onTableClick);
    els.role.addEventListener("change", function () {
      toggleRoleFields(els.role.value);
    });

    els.closeDialogBtn.addEventListener("click", function () {
      closeDialog({ notifyCancel: true });
    });
    els.cancelDialogBtn.addEventListener("click", function () {
      closeDialog({ notifyCancel: true });
    });
    els.closeDeleteBtn.addEventListener("click", function () {
      closeDeleteDialog({ notifyCancel: true });
    });
    els.cancelDeleteBtn.addEventListener("click", function () {
      closeDeleteDialog({ notifyCancel: true });
    });
    els.confirmDeleteBtn.addEventListener("click", confirmDelete);
    els.notificationClose.addEventListener("click", hideNotification);
  }

  async function initialize() {
    if (state.initialized) {
      return;
    }
    state.initialized = true;

    if (!hasAllRequiredElements()) {
      return;
    }

    bindEvents();
    toggleRoleFields(els.role.value);
    await loadUsers();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initialize);
  } else {
    initialize();
  }
})();
