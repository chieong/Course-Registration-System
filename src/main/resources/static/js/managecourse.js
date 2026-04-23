let courses = [];
let editingCode = null;
let deletingCode = null;
let notificationTimer = null;
let notificationHideTimer = null;
let pendingUnloadWarning = false;
let initialCoursesSignature = "[]";

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

function showActionMessage(message, type) {
  var msgEl = document.getElementById("mcActionMessage");
  var msgTextEl = document.getElementById("mcActionMessageText");
  if (!msgEl || !msgTextEl) return;

  msgTextEl.textContent = message;
  msgEl.classList.remove("mc-notification-success", "mc-notification-error", "mc-notification-info");
  if (type === "error") {
    msgEl.classList.add("mc-notification-error");
  } else if (type === "info") {
    msgEl.classList.add("mc-notification-info");
  } else {
    msgEl.classList.add("mc-notification-success");
  }

  showNotification();
  clearTimeout(notificationTimer);
  notificationTimer = setTimeout(function () {
    hideNotification();
  }, 4000);
}

function hideNotification() {
  var msgEl = document.getElementById("mcActionMessage");
  if (!msgEl) return;
  clearTimeout(notificationHideTimer);
  msgEl.classList.remove("is-visible");
  msgEl.classList.add("is-hiding");
  notificationHideTimer = setTimeout(function () {
    msgEl.classList.add("is-hidden");
    msgEl.classList.remove("is-hiding");
    msgEl.setAttribute("hidden", "hidden");
  }, 230);
}

function showNotification() {
  var msgEl = document.getElementById("mcActionMessage");
  if (!msgEl) return;
  clearTimeout(notificationHideTimer);
  msgEl.removeAttribute("hidden");
  msgEl.classList.remove("is-hiding");
  msgEl.classList.remove("is-hidden");
  void msgEl.offsetWidth;
  msgEl.classList.add("is-visible");
}

function hasUnsavedCourseChanges() {
  return JSON.stringify(courses) !== initialCoursesSignature;
}

function getEnrollmentStatus(enrolled, maxEnroll, waitlistSize) {
  if (enrolled < maxEnroll) return "open";
  if (waitlistSize > 0) return "waitlist";
  if (enrolled >= maxEnroll) return "full";
  return "open";
}

function categoryPillClass(category) {
  if (category === "Core") return "pill pill-core";
  if (category === "Lab") return "pill pill-lab";
  return "pill pill-elective";
}

function updateStats() {
  document.getElementById("statTotal").textContent = courses.length;
  document.getElementById("statCore").textContent = courses.filter(function (c) { return c.category === "Core"; }).length;
  document.getElementById("statElective").textContent = courses.filter(function (c) { return c.category === "Elective"; }).length;
  document.getElementById("statLab").textContent = courses.filter(function (c) { return c.category === "Lab"; }).length;
}

function renderTable() {
  var search = document.getElementById("mcSearch").value.trim().toLowerCase();
  var cat = document.getElementById("mcCategory").value;
  var dept = document.getElementById("mcDept").value;

  var filtered = courses.filter(function (c) {
    var matchSearch = !search || c.code.toLowerCase().indexOf(search) !== -1 || c.title.toLowerCase().indexOf(search) !== -1;
    var matchCat = !cat || c.category === cat;
    var matchDept = !dept || c.dept === dept;
    return matchSearch && matchCat && matchDept;
  });

  var tbody = document.getElementById("courseTableBody");

  if (filtered.length === 0) {
    tbody.innerHTML = "<tr class=\"empty-row\"><td colspan=\"12\">No courses found.</td></tr>";
    return;
  }

  tbody.innerHTML = filtered.map(function (c) {
    var status = getEnrollmentStatus(c.enrolled, c.maxEnroll, c.waitlistSize);
    var statusLabel = status === "full" ? "Full" : status === "waitlist" ? "Waitlist" : "Open";
    var statusClass = "mc-status-" + status;
    var waitlistSize = Number(c.waitlistSize || 0);
    var waitlistCurrent = Number(c.waitlistCurrent || 0);
    return "<tr>"
      + "<td><strong>" + c.code + "</strong></td>"
      + "<td>" + c.title + "</td>"
      + "<td><span class=\"" + categoryPillClass(c.category) + "\">" + c.category + "</span></td>"
      + "<td>" + c.credits + "</td>"
      + "<td>" + c.dept + "</td>"
      + "<td>" + c.day + "</td>"
      + "<td>" + c.startTime + " - " + c.endTime + "</td>"
      + "<td>" + c.location + "</td>"
      + "<td>" + waitlistSize + "</td>"
      + "<td>" + waitlistCurrent + "</td>"
      + "<td>" + c.enrolled + " / " + c.maxEnroll + "<span class=\"" + statusClass + "\">" + statusLabel + "</span></td>"
      + "<td><div class=\"inline-actions\">"
      + "<button type=\"button\" class=\"small-btn\" data-edit=\"" + c.code + "\">Edit</button>"
      + "<button type=\"button\" class=\"small-danger\" data-delete=\"" + c.code + "\">Delete</button>"
      + "</div></td>"
      + "</tr>";
  }).join("");
}

function mapCourseFromApi(course) {
  return {
    code: course.courseCode || "",
    title: course.title || "",
    category: "Core",
    credits: Number(course.credits || 0),
    dept: "Computer Science",
    day: "TBA",
    startTime: "00:00",
    endTime: "00:00",
    location: "TBA",
    enrolled: 0,
    maxEnroll: 0,
    waitlistSize: 0,
    waitlistCurrent: 0,
    description: course.description || ""
  };
}

async function refreshCourses() {
  const data = await apiRequest("/api/admin/courses", { method: "GET" });
  courses = Array.isArray(data) ? data.map(mapCourseFromApi) : [];
  initialCoursesSignature = JSON.stringify(courses);
  updateStats();
  renderTable();
}

document.getElementById("mcSearch").addEventListener("input", renderTable);
document.getElementById("mcCategory").addEventListener("change", renderTable);
document.getElementById("mcDept").addEventListener("change", renderTable);

document.getElementById("courseTableBody").addEventListener("click", function (e) {
  var editBtn = e.target.closest("[data-edit]");
  var deleteBtn = e.target.closest("[data-delete]");
  if (editBtn) openEditDialog(editBtn.dataset.edit);
  if (deleteBtn) openDeleteDialog(deleteBtn.dataset.delete);
});

var courseDialog = document.getElementById("courseDialog");

document.getElementById("addCourseBtn").addEventListener("click", openAddDialog);
document.getElementById("closeDialogBtn").addEventListener("click", function () {
  courseDialog.close();
  showActionMessage(editingCode ? "Course modification cancelled." : "Course creation cancelled.", "info");
});
document.getElementById("cancelDialogBtn").addEventListener("click", function () {
  courseDialog.close();
  showActionMessage(editingCode ? "Course modification cancelled." : "Course creation cancelled.", "info");
});

function clearFormError() {
  var errorEl = document.getElementById("formError");
  errorEl.textContent = "";
  errorEl.classList.add("is-hidden");
}

function openAddDialog() {
  editingCode = null;
  document.getElementById("dialogTitle").textContent = "Add New Course";
  document.getElementById("saveBtn").textContent = "Add Course";
  document.getElementById("courseForm").reset();
  document.getElementById("fCode").readOnly = false;
  document.getElementById("fWaitlistSize").value = "0";
  clearFormError();
  courseDialog.showModal();
}

function openEditDialog(code) {
  var c = courses.find(function (x) { return x.code === code; });
  if (!c) return;
  editingCode = code;
  document.getElementById("dialogTitle").textContent = "Edit Course";
  document.getElementById("saveBtn").textContent = "Save Changes";
  document.getElementById("fCode").value = c.code;
  document.getElementById("fCode").readOnly = true;
  document.getElementById("fTitle").value = c.title;
  document.getElementById("fCategory").value = c.category;
  document.getElementById("fCredits").value = c.credits;
  document.getElementById("fDept").value = c.dept;
  document.getElementById("fDay").value = c.day;
  document.getElementById("fStartTime").value = c.startTime === "00:00" ? "" : c.startTime;
  document.getElementById("fEndTime").value = c.endTime === "00:00" ? "" : c.endTime;
  document.getElementById("fLocation").value = c.location === "TBA" ? "" : c.location;
  document.getElementById("fMaxEnroll").value = c.maxEnroll;
  document.getElementById("fWaitlistSize").value = Number(c.waitlistSize || 0);
  document.getElementById("fEnrolled").value = c.enrolled;
  clearFormError();
  courseDialog.showModal();
}

document.getElementById("courseForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  var code = document.getElementById("fCode").value.trim().toUpperCase();
  var title = document.getElementById("fTitle").value.trim();
  var category = document.getElementById("fCategory").value;
  var creditsRaw = document.getElementById("fCredits").value;
  var credits = parseInt(creditsRaw, 10);

  var errorEl = document.getElementById("formError");

  function showError(msg) {
    errorEl.textContent = msg;
    errorEl.classList.remove("is-hidden");
  }

  if (!code || !title || !category || !creditsRaw) {
    showError("Please provide at least code, title, category, and credits.");
    return;
  }
  if (isNaN(credits) || credits < 1) {
    showError("Credits must be a number of 1 or more.");
    return;
  }

  if (editingCode) {
    var confirmModify = window.confirm("Confirm updating course " + code + "?");
    if (!confirmModify) {
      showActionMessage("Course modification cancelled.", "info");
      return;
    }
  }

  const payload = {
    courseCode: code,
    title: title,
    credits: credits,
    description: document.getElementById("fLocation").value.trim() || null
  };

  try {
    if (editingCode) {
      await apiRequest("/api/admin/course", {
        method: "PUT",
        body: JSON.stringify(payload)
      });
      showActionMessage("Course " + code + " updated successfully.", "success");
    } else {
      await apiRequest("/api/admin/courses", {
        method: "POST",
        body: JSON.stringify(payload)
      });
      showActionMessage("Course " + code + " added successfully.", "success");
    }

    courseDialog.close();
    await refreshCourses();
  } catch (requestError) {
    showError(requestError.message || "Failed to save course.");
  }
});

var deleteDialog = document.getElementById("deleteDialog");

document.getElementById("closeDeleteBtn").addEventListener("click", function () {
  deleteDialog.close();
  deletingCode = null;
  showActionMessage("Course removal cancelled.", "info");
});
document.getElementById("cancelDeleteBtn").addEventListener("click", function () {
  deleteDialog.close();
  deletingCode = null;
  showActionMessage("Course removal cancelled.", "info");
});

function openDeleteDialog(code) {
  var c = courses.find(function (x) { return x.code === code; });
  if (!c) return;
  deletingCode = code;
  document.getElementById("deleteMsg").textContent = "Are you sure you want to delete \"" + c.code + " - " + c.title + "\"? This action cannot be undone.";
  deleteDialog.showModal();
}

document.getElementById("confirmDeleteBtn").addEventListener("click", async function () {
  if (!deletingCode) {
    deleteDialog.close();
    return;
  }

  var removedCode = deletingCode;

  try {
    await apiRequest("/api/admin/course/" + encodeURIComponent(removedCode), {
      method: "DELETE"
    });

    deletingCode = null;
    deleteDialog.close();
    await refreshCourses();
    showActionMessage("Course " + removedCode + " removed successfully.", "success");
  } catch (requestError) {
    showActionMessage(requestError.message || "Failed to remove course.", "error");
  }
});

document.getElementById("mcActionMessageClose").addEventListener("click", function () {
  hideNotification();
  clearTimeout(notificationTimer);
});

window.addEventListener("beforeunload", function (event) {
  if (!hasUnsavedCourseChanges()) return;
  pendingUnloadWarning = true;
  event.preventDefault();
  event.returnValue = "";
});

window.addEventListener("focus", function () {
  if (!pendingUnloadWarning) return;
  pendingUnloadWarning = false;
  if (hasUnsavedCourseChanges()) {
    showActionMessage("Page close/navigation cancelled. Unsaved course modifications are kept.", "info");
  }
});

async function initPage() {
  hideNotification();

  try {
    await refreshCourses();
    showActionMessage("Courses loaded from server.", "info");
  } catch (error) {
    document.getElementById("courseTableBody").innerHTML = "<tr class=\"empty-row\"><td colspan=\"12\">Failed to load courses.</td></tr>";
    showActionMessage(error.message || "Failed to load courses.", "error");
  }
}

initPage();
