const INITIAL_COURSES = [
	{ code: "CSC201", title: "Programming Fundamentals Lab",      category: "Lab",      credits: 1, dept: "Computer Science", day: "Tuesday",   startTime: "11:00", endTime: "12:00", location: "Room E205",        enrolled: 18, maxEnroll: 20, waitlistSize: 5, waitlistCurrent: 1 },
	{ code: "CSC302", title: "Data Structures and Algorithms II", category: "Core",     credits: 3, dept: "Computer Science", day: "Monday",    startTime: "09:00", endTime: "11:00", location: "Room A101",        enrolled: 25, maxEnroll: 30, waitlistSize: 8, waitlistCurrent: 0 },
	{ code: "CSC311", title: "Operating Systems",                 category: "Core",     credits: 3, dept: "Computer Science", day: "Monday",    startTime: "10:00", endTime: "12:00", location: "Room G102",        enrolled: 22, maxEnroll: 30, waitlistSize: 8, waitlistCurrent: 0 },
	{ code: "CSC318", title: "Software Engineering Practices",    category: "Core",     credits: 3, dept: "Computer Science", day: "Tuesday",   startTime: "14:00", endTime: "16:00", location: "Room B204",        enrolled: 28, maxEnroll: 30, waitlistSize: 8, waitlistCurrent: 0 },
	{ code: "CSC325", title: "Information Security",              category: "Elective", credits: 3, dept: "Computer Science", day: "Tuesday",   startTime: "14:00", endTime: "16:00", location: "Room D108",        enrolled: 30, maxEnroll: 30, waitlistSize: 12, waitlistCurrent: 6 },
	{ code: "CSC331", title: "Database Systems",                  category: "Core",     credits: 3, dept: "Computer Science", day: "Tuesday",   startTime: "10:00", endTime: "12:00", location: "Room H205",        enrolled: 18, maxEnroll: 30, waitlistSize: 8, waitlistCurrent: 0 },
	{ code: "CSC344", title: "Computer Graphics",                 category: "Elective", credits: 3, dept: "Computer Science", day: "Wednesday", startTime: "11:00", endTime: "13:00", location: "Room B110",        enrolled: 12, maxEnroll: 25, waitlistSize: 6, waitlistCurrent: 0 },
	{ code: "CSC352", title: "Human Computer Interaction",        category: "Elective", credits: 3, dept: "Computer Science", day: "Wednesday", startTime: "10:00", endTime: "12:00", location: "Room C310",        enrolled: 20, maxEnroll: 30, waitlistSize: 8, waitlistCurrent: 0 },
	{ code: "CSC360", title: "Cloud Application Development",     category: "Elective", credits: 3, dept: "Computer Science", day: "Thursday",  startTime: "09:00", endTime: "11:00", location: "Lab 2",            enrolled: 30, maxEnroll: 30, waitlistSize: 15, waitlistCurrent: 9 },
	{ code: "CSC384", title: "Computer Networks Lab",             category: "Lab",      credits: 1, dept: "Computer Science", day: "Friday",    startTime: "13:00", endTime: "15:00", location: "Networks Lab",     enrolled: 15, maxEnroll: 20, waitlistSize: 5, waitlistCurrent: 0 },
	{ code: "CSC390", title: "AI Applications Workshop",          category: "Elective", credits: 2, dept: "Computer Science", day: "Saturday",  startTime: "10:00", endTime: "12:00", location: "Innovation Studio", enrolled: 24, maxEnroll: 30, waitlistSize: 10, waitlistCurrent: 2 },
	{ code: "CSC415", title: "Advanced Algorithms",               category: "Core",     credits: 3, dept: "Computer Science", day: "Thursday",  startTime: "13:00", endTime: "16:00", location: "Room F301",        enrolled: 20, maxEnroll: 30, waitlistSize: 8, waitlistCurrent: 0 },
	{ code: "CSC422", title: "Machine Learning",                  category: "Elective", credits: 3, dept: "Computer Science", day: "Thursday",  startTime: "14:00", endTime: "16:00", location: "Room C220",        enrolled: 24, maxEnroll: 30, waitlistSize: 10, waitlistCurrent: 0 },
];

let courses = INITIAL_COURSES.map(function (c) { return Object.assign({}, c); });
let editingCode = null;
let deletingCode = null;
let notificationTimer = null;
let notificationHideTimer = null;
let pendingUnloadWarning = false;
const initialCoursesSignature = JSON.stringify(courses);

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
			+ "<td>" + c.startTime + " \u2013 " + c.endTime + "</td>"
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

document.getElementById("mcSearch").addEventListener("input", renderTable);
document.getElementById("mcCategory").addEventListener("change", renderTable);
document.getElementById("mcDept").addEventListener("change", renderTable);

document.getElementById("courseTableBody").addEventListener("click", function (e) {
	var editBtn = e.target.closest("[data-edit]");
	var deleteBtn = e.target.closest("[data-delete]");
	if (editBtn) openEditDialog(editBtn.dataset.edit);
	if (deleteBtn) openDeleteDialog(deleteBtn.dataset.delete);
});

// ---- Add / Edit Dialog ----
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
	document.getElementById("fStartTime").value = c.startTime;
	document.getElementById("fEndTime").value = c.endTime;
	document.getElementById("fLocation").value = c.location;
	document.getElementById("fMaxEnroll").value = c.maxEnroll;
	document.getElementById("fWaitlistSize").value = Number(c.waitlistSize || 0);
	document.getElementById("fEnrolled").value = c.enrolled;
	clearFormError();
	courseDialog.showModal();
}

document.getElementById("courseForm").addEventListener("submit", function (e) {
	e.preventDefault();

	var code = document.getElementById("fCode").value.trim().toUpperCase();
	var title = document.getElementById("fTitle").value.trim();
	var category = document.getElementById("fCategory").value;
	var creditsRaw = document.getElementById("fCredits").value;
	var dept = document.getElementById("fDept").value;
	var day = document.getElementById("fDay").value;
	var startTime = document.getElementById("fStartTime").value;
	var endTime = document.getElementById("fEndTime").value;
	var location = document.getElementById("fLocation").value.trim();
	var maxEnrollRaw = document.getElementById("fMaxEnroll").value;
	var waitlistSizeRaw = document.getElementById("fWaitlistSize").value;
	var enrolledRaw = document.getElementById("fEnrolled").value;

	var credits = parseInt(creditsRaw, 10);
	var maxEnroll = parseInt(maxEnrollRaw, 10);
	var waitlistSize = parseInt(waitlistSizeRaw, 10);
	var enrolled = parseInt(enrolledRaw, 10);

	var errorEl = document.getElementById("formError");

	function showError(msg) {
		errorEl.textContent = msg;
		errorEl.classList.remove("is-hidden");
	}

	if (!code || !title || !category || !creditsRaw || !dept || !day || !startTime || !endTime || !location || !maxEnrollRaw || waitlistSizeRaw === "" || !enrolledRaw) {
		showError("Please fill in all fields.");
		return;
	}
	if (isNaN(credits) || credits < 1) {
		showError("Credits must be a number of 1 or more.");
		return;
	}
	if (isNaN(maxEnroll) || maxEnroll < 1) {
		showError("Max enrollment must be a number of 1 or more.");
		return;
	}
	if (isNaN(waitlistSize) || waitlistSize < 0) {
		showError("Waitlist size must be 0 or more.");
		return;
	}
	if (isNaN(enrolled) || enrolled < 0) {
		showError("Currently enrolled must be 0 or more.");
		return;
	}
	if (startTime >= endTime) {
		showError("End time must be after start time.");
		return;
	}
	if (enrolled > maxEnroll) {
		showError("Currently enrolled cannot exceed max enrollment.");
		return;
	}
	if (!editingCode && courses.some(function (c) { return c.code === code; })) {
		showError("Course code \"" + code + "\" already exists.");
		return;
	}

	if (editingCode) {
		var confirmModify = window.confirm("Confirm updating course " + code + "?");
		if (!confirmModify) {
			showActionMessage("Course modification cancelled.", "info");
			return;
		}
	}

	var existing = editingCode ? courses.find(function (c) { return c.code === editingCode; }) : null;
	var updated = {
		code: code,
		title: title,
		category: category,
		credits: credits,
		dept: dept,
		day: day,
		startTime: startTime,
		endTime: endTime,
		location: location,
		maxEnroll: maxEnroll,
		waitlistSize: waitlistSize,
		waitlistCurrent: existing ? Number(existing.waitlistCurrent || 0) : 0,
		enrolled: enrolled
	};

	if (editingCode) {
		var idx = courses.findIndex(function (c) { return c.code === editingCode; });
		courses[idx] = updated;
		showActionMessage("Course " + code + " updated successfully.", "success");
	} else {
		courses.push(updated);
		showActionMessage("Course " + code + " added successfully.", "success");
	}

	courseDialog.close();
	updateStats();
	renderTable();
});

// ---- Delete Dialog ----
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
	if (Number(c.enrolled || 0) > 0 || Number(c.waitlistCurrent || 0) > 0) {
		showActionMessage("Cannot remove " + c.code + ". The course still has enrolled or waitlisted students.", "error");
		return;
	}
	deletingCode = code;
	document.getElementById("deleteMsg").textContent = "Are you sure you want to delete \"" + c.code + " - " + c.title + "\"? This action cannot be undone.";
	deleteDialog.showModal();
}

document.getElementById("confirmDeleteBtn").addEventListener("click", function () {
	if (!deletingCode) {
		deleteDialog.close();
		return;
	}
	var removedCode = deletingCode;
	courses = courses.filter(function (c) { return c.code !== deletingCode; });
	deletingCode = null;
	deleteDialog.close();
	updateStats();
	renderTable();
	showActionMessage("Course " + removedCode + " removed successfully.", "success");
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

// Initial render
hideNotification();
updateStats();
renderTable();
