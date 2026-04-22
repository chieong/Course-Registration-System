(function () {
  const state = {
    users: [],
    editingId: null,
    deletingId: null,
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
    deleteMsg: document.getElementById("muDeleteMsg"),
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
      id: admin.staffId,
      username: admin.userEID || "",
      fullName: admin.userName || "",
      email: admin.userEID ? (admin.userEID + "@cityu.local") : "",
      role: "Admin",
      status: "Active",
      password: ""
    };
  }

  async function loadUsers() {
    const data = await apiRequest("/api/admin/users", { method: "GET" });
    state.users = Array.isArray(data) ? data.map(mapAdminToUi) : [];
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

    els.statTotal.textContent = String(total);
    els.statActive.textContent = String(active);
    els.statStudent.textContent = "0";
    els.statStaff.textContent = String(total);
  }

  function renderTable() {
    const rows = getFilteredUsers();

    if (!rows.length) {
      els.tableBody.innerHTML = "<tr class=\"empty-row\"><td colspan=\"6\">No users found for the selected filters.</td></tr>";
      return;
    }

    els.tableBody.innerHTML = rows.map(function (user) {
      return "\n        <tr>\n          <td>" + escapeHtml(user.username) + "</td>\n          <td>" + escapeHtml(user.fullName) + "</td>\n          <td>" + escapeHtml(user.email) + "</td>\n          <td>" + escapeHtml(user.role) + "</td>\n          <td><span class=\"mu-status-active\">Active</span></td>\n          <td>\n            <div class=\"inline-actions\">\n              <button type=\"button\" class=\"small-btn\" data-action=\"edit\" data-id=\"" + user.id + "\">Edit</button>\n              <button type=\"button\" class=\"small-danger\" data-action=\"delete\" data-id=\"" + user.id + "\">Delete</button>\n            </div>\n          </td>\n        </tr>\n      ";
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

  function resetForm() {
    state.editingId = null;
    els.dialogTitle.textContent = "Add User";
    els.saveBtn.textContent = "Add User";
    els.form.reset();
    els.status.value = "Active";
    els.role.value = "Admin";
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

  function openEditDialog(id) {
    const user = state.users.find(function (u) { return u.id === id; });
    if (!user) {
      return;
    }

    state.editingId = id;
    els.dialogTitle.textContent = "Edit User";
    els.saveBtn.textContent = "Save Changes";
    els.username.value = user.username;
    els.fullName.value = user.fullName;
    els.email.value = user.email;
    els.role.value = "Admin";
    els.status.value = "Active";
    els.password.value = "";
    els.username.readOnly = false;
    clearFormError();
    openModal(els.dialog);
  }

  function closeDialog(options) {
    const notify = !!(options && options.notifyCancel);
    closeModal(els.dialog);
    if (notify) {
      showMessage(state.editingId ? "User modification cancelled." : "User creation cancelled.", "info");
    }
  }

  function openDeleteDialog(id) {
    const user = state.users.find(function (u) { return u.id === id; });
    if (!user) {
      return;
    }

    state.deletingId = id;
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
    state.deletingId = null;
    closeModal(els.deleteDialog);
    if (notify) {
      showMessage("User removal cancelled.", "info");
    }
  }

  function validateForm() {
    const username = els.username.value.trim();
    const fullName = els.fullName.value.trim();
    const email = els.email.value.trim();
    const password = els.password.value;

    if (!username || !fullName || !email) {
      return "Please fill in all required fields.";
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return "Please enter a valid email address.";
    }

    if (!state.editingId && password.trim().length < 8) {
      return "Temporary password must be at least 8 characters for new users.";
    }

    return "";
  }

  async function saveUser(event) {
    event.preventDefault();
    clearFormError();

    const error = validateForm();
    if (error) {
      showFormError(error);
      return;
    }

    const isEdit = !!state.editingId;
    const confirmed = window.confirm(isEdit
      ? "Are you sure you want to save changes to this user account?"
      : "Are you sure you want to create this user account?");

    if (!confirmed) {
      showMessage(isEdit ? "User modification cancelled." : "User creation cancelled.", "info");
      return;
    }

    const payload = {
      userEID: els.username.value.trim(),
      name: els.fullName.value.trim(),
      password: els.password.value || null
    };

    try {
      if (isEdit) {
        await apiRequest("/api/admin/users/" + state.editingId, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      } else {
        await apiRequest("/api/admin/users", {
          method: "POST",
          body: JSON.stringify(payload)
        });
      }

      closeModal(els.dialog);
      await loadUsers();
      showMessage(isEdit ? "User account updated successfully." : "New user account created successfully.", "success");
    } catch (requestError) {
      showFormError(requestError.message || "Failed to save user.");
    }
  }

  async function confirmDelete() {
    if (state.deletingId == null) {
      return;
    }

    const user = state.users.find(function (u) { return u.id === state.deletingId; });
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
      await apiRequest("/api/admin/users/" + state.deletingId, { method: "DELETE" });
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

    const id = Number(button.getAttribute("data-id"));
    if (!id) {
      return;
    }

    const action = button.getAttribute("data-action");
    if (action === "edit") {
      openEditDialog(id);
      return;
    }
    if (action === "delete") {
      openDeleteDialog(id);
    }
  }

  function bindEvents() {
    els.search.addEventListener("input", renderTable);
    els.roleFilter.addEventListener("change", renderTable);
    els.statusFilter.addEventListener("change", renderTable);

    els.addBtn.addEventListener("click", openCreateDialog);
    els.form.addEventListener("submit", saveUser);
    els.tableBody.addEventListener("click", onTableClick);

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

    els.dialog.addEventListener("close", clearFormError);
    els.notificationClose.addEventListener("click", function () {
      hideNotification();
      clearTimeout(state.notificationTimer);
    });
  }

  async function init() {
    if (!hasAllRequiredElements() || state.initialized) {
      return;
    }

    state.initialized = true;
    hideNotification();
    bindEvents();

    try {
      await loadUsers();
      showMessage("Users loaded from server.", "info");
    } catch (error) {
      els.tableBody.innerHTML = "<tr class=\"empty-row\"><td colspan=\"6\">Failed to load users.</td></tr>";
      showMessage(error.message || "Failed to load users.", "error");
    }
  }

  init();
})();
