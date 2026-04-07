(function () {
  const state = {
    users: [
      {
        id: 1,
        username: "s2201001",
        fullName: "Chris Chan",
        email: "s2201001@my.cityu.edu.hk",
        role: "Student",
        status: "Active",
        password: ""
      },
      {
        id: 2,
        username: "s2201045",
        fullName: "Mandy Lee",
        email: "s2201045@my.cityu.edu.hk",
        role: "Student",
        status: "Disabled",
        password: ""
      },
      {
        id: 3,
        username: "staff001",
        fullName: "Jason Ng",
        email: "jason.ng@cityu.edu.hk",
        role: "Staff",
        status: "Active",
        password: ""
      },
      {
        id: 4,
        username: "admin001",
        fullName: "Alice Ho",
        email: "alice.ho@cityu.edu.hk",
        role: "Admin",
        status: "Active",
        password: ""
      }
    ],
    editingId: null,
    deletingId: null,
    notificationTimer: null,
    notificationHideTimer: null
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

  function openModal(dialog) {
    if (!dialog) {
      return;
    }

    if (typeof dialog.showModal === "function") {
      dialog.showModal();
      return;
    }

    dialog.setAttribute("open", "open");
  }

  function closeModal(dialog) {
    if (!dialog) {
      return;
    }

    if (typeof dialog.close === "function") {
      dialog.close();
      return;
    }

    dialog.removeAttribute("open");
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

  function getFilteredUsers() {
    const search = normalize(els.search.value);
    const roleFilter = els.roleFilter.value;
    const statusFilter = els.statusFilter.value;

    return state.users.filter((user) => {
      const matchesSearch = !search || [user.username, user.fullName, user.email]
        .some((field) => normalize(field).includes(search));
      const matchesRole = !roleFilter || user.role === roleFilter;
      const matchesStatus = !statusFilter || user.status === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });
  }

  function renderStats() {
    const total = state.users.length;
    const active = state.users.filter((u) => u.status === "Active").length;
    const students = state.users.filter((u) => u.role === "Student").length;
    const staff = state.users.filter((u) => u.role === "Staff" || u.role === "Admin").length;

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

    els.tableBody.innerHTML = rows.map((user) => {
      const statusClass = user.status === "Active" ? "mu-status-active" : "mu-status-disabled";

      return `
        <tr>
          <td>${escapeHtml(user.username)}</td>
          <td>${escapeHtml(user.fullName)}</td>
          <td>${escapeHtml(user.email)}</td>
          <td>${escapeHtml(user.role)}</td>
          <td><span class="${statusClass}">${escapeHtml(user.status)}</span></td>
          <td>
            <div class="inline-actions">
              <button type="button" class="small-btn" data-action="edit" data-id="${user.id}">Edit</button>
              <button type="button" class="small-danger" data-action="delete" data-id="${user.id}">Delete</button>
            </div>
          </td>
        </tr>
      `;
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
    // Force a reflow so visibility transition runs every time.
    void els.notification.offsetWidth;
    els.notification.classList.add("is-visible");
  }

  function showSuccess(message) {
    els.notificationText.textContent = message;
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
    els.username.readOnly = false;
    clearFormError();
  }

  function openCreateDialog() {
    resetForm();
    openModal(els.dialog);
  }

  function openEditDialog(id) {
    const user = state.users.find((u) => u.id === id);
    if (!user) {
      return;
    }

    state.editingId = id;
    els.dialogTitle.textContent = "Edit User";
    els.saveBtn.textContent = "Save Changes";
    els.username.value = user.username;
    els.fullName.value = user.fullName;
    els.email.value = user.email;
    els.role.value = user.role;
    els.status.value = user.status;
    els.password.value = "";
    els.username.readOnly = true;
    clearFormError();
    openModal(els.dialog);
  }

  function closeDialog() {
    if (els.dialog.open) {
      closeModal(els.dialog);
    }
  }

  function openDeleteDialog(id) {
    const user = state.users.find((u) => u.id === id);
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
      return `<div class="mu-detail-row"><span class="mu-detail-label">${escapeHtml(pair[0])}</span><span>${escapeHtml(pair[1])}</span></div>`;
    }).join("");
    openModal(els.deleteDialog);
  }

  function closeDeleteDialog() {
    state.deletingId = null;
    if (els.deleteDialog.open) {
      closeModal(els.deleteDialog);
    }
  }

  function validateForm() {
    const username = els.username.value.trim();
    const fullName = els.fullName.value.trim();
    const email = els.email.value.trim();
    const role = els.role.value;
    const status = els.status.value;
    const password = els.password.value;

    if (!username || !fullName || !email || !role || !status) {
      return "Please fill in all required fields.";
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return "Please enter a valid email address.";
    }

    if (!state.editingId && password.trim().length < 8) {
      return "Temporary password must be at least 8 characters for new users.";
    }

    const duplicateUser = state.users.find(
      (u) => normalize(u.username) === normalize(username) && u.id !== state.editingId
    );

    if (duplicateUser) {
      return "Username already exists. Please use a different username.";
    }

    return "";
  }

  function saveUser(event) {
    event.preventDefault();
    clearFormError();

    const error = validateForm();
    if (error) {
      showFormError(error);
      return;
    }

    const payload = {
      username: els.username.value.trim(),
      fullName: els.fullName.value.trim(),
      email: els.email.value.trim(),
      role: els.role.value,
      status: els.status.value,
      password: els.password.value
    };

    if (state.editingId) {
      const index = state.users.findIndex((u) => u.id === state.editingId);
      if (index >= 0) {
        state.users[index] = {
          ...state.users[index],
          ...payload,
          password: ""
        };
      }
    } else {
      const nextId = Math.max(0, ...state.users.map((u) => u.id)) + 1;
      state.users.push({ id: nextId, ...payload, password: "" });
    }

    const isEdit = !!state.editingId;
    closeDialog();
    renderAll();
    showSuccess(isEdit ? "User account updated successfully." : "New user account created successfully.");
  }

  function confirmDelete() {
    if (state.deletingId == null) {
      return;
    }

    state.users = state.users.filter((u) => u.id !== state.deletingId);
    closeDeleteDialog();
    renderAll();
    showSuccess("User account removed successfully.");
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

    els.closeDialogBtn.addEventListener("click", closeDialog);
    els.cancelDialogBtn.addEventListener("click", closeDialog);
    els.closeDeleteBtn.addEventListener("click", closeDeleteDialog);
    els.cancelDeleteBtn.addEventListener("click", closeDeleteDialog);
    els.confirmDeleteBtn.addEventListener("click", confirmDelete);

    els.dialog.addEventListener("close", clearFormError);
    els.notificationClose.addEventListener("click", function () {
      hideNotification();
      clearTimeout(state.notificationTimer);
    });
  }

  if (!hasAllRequiredElements()) {
    return;
  }

  hideNotification();
  bindEvents();
  renderAll();
})();
