const availableCoursesBody = document.getElementById("availableCoursesBody");
const selectedPlanBody = document.getElementById("selectedPlanBody");
const waitlistBody = document.getElementById("waitlistBody");
const creditValue = document.getElementById("creditValue");
const creditStatus = document.getElementById("creditStatus");
const submitPlanBtn = document.getElementById("submitPlanBtn");
const undoPlanBtn = document.getElementById("undoPlanBtn");
const previewTimetableBody = document.getElementById("previewTimetableBody");
const conflictPanel = document.getElementById("conflictPanel");
const conflictList = document.getElementById("conflictList");
const noConflictPanel = document.getElementById("noConflictPanel");
const availableCourseSearch = document.getElementById("availableCourseSearch");
const prevPlanBtn = document.getElementById("prevPlanBtn");
const nextPlanBtn = document.getElementById("nextPlanBtn");
const addPlanBtn = document.getElementById("addPlanBtn");
const removePlanBtn = document.getElementById("removePlanBtn");
const planPager = document.getElementById("planPager");
const summaryStudent = document.getElementById("summaryStudent");
const summaryProgramme = document.getElementById("summaryProgramme");
const summaryStudyYear = document.getElementById("summaryStudyYear");
const summaryAcademicTerm = document.getElementById("summaryAcademicTerm");
const academicTermSelect = document.getElementById("academicTerm");

const previewDays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const previewStartHour = 9;
const previewEndHour = 22;

let currentStudentId = null;
let availableSections = [];
let plans = [];
let currentPlanIndex = 0;

function renderStudentSummary(session) {
  if (summaryStudent) {
    const displayName = asText(session && session.displayName, "Student");
    const studentIdText = session && session.studentId ? ` (${session.studentId})` : "";
    summaryStudent.textContent = `${displayName}${studentIdText}`;
  }

  if (summaryProgramme) {
    summaryProgramme.textContent = "Computer Science";
  }

  if (summaryStudyYear) {
    summaryStudyYear.textContent = asText(session && session.role, "STUDENT");
  }

  if (summaryAcademicTerm) {
    const selectedTerm = academicTermSelect && academicTermSelect.selectedOptions.length
      ? academicTermSelect.selectedOptions[0].textContent
      : "Current Term";
    summaryAcademicTerm.textContent = asText(selectedTerm, "Current Term");
  }
}

function asText(value, fallback = "-") {
  if (value === null || value === undefined) {
    return fallback;
  }

  const text = String(value).trim();
  return text.length === 0 ? fallback : text;
}

function toDateTime(dateTimeText) {
  if (!dateTimeText || typeof dateTimeText !== "string") {
    return null;
  }

  const date = new Date(dateTimeText.replace(" ", "T"));
  return Number.isNaN(date.getTime()) ? null : date;
}

function dayFromDateTime(dateTimeText) {
  const date = toDateTime(dateTimeText);
  if (!date) {
    return "TBA";
  }

  return date.toLocaleDateString("en-US", { weekday: "long" });
}

function timeFromDateTime(dateTimeText) {
  const date = toDateTime(dateTimeText);
  if (!date) {
    return "00:00";
  }

  return date.toLocaleTimeString("en-GB", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false
  });
}

function toTimeRange(startTime, endTime) {
  return `${startTime} - ${endTime}`;
}

function categoryFromType(type) {
  const normalized = String(type || "").toUpperCase();
  if (normalized === "LAB") {
    return "Lab";
  }
  if (normalized === "TUTORIAL") {
    return "Elective";
  }
  return "Core";
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

function getCurrentPlan() {
  if (!plans.length) {
    return null;
  }
  return plans[currentPlanIndex];
}

function getPlanSignature(plan) {
  if (!plan) {
    return "[]";
  }

  const simplified = (plan.entries || [])
    .map((entry) => ({
      entryId: entry.entryId,
      sectionId: entry.sectionId,
      entryType: entry.entryType,
      status: entry.status
    }))
    .sort((a, b) => Number(a.entryId) - Number(b.entryId));

  return JSON.stringify(simplified);
}

function hasUnsavedPlanChanges() {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return false;
  }

  return getPlanSignature(currentPlan) !== currentPlan.savedSignature;
}

function updateCreditStatus(totalCredits) {
  creditValue.textContent = String(totalCredits);

  creditStatus.classList.remove("status-good", "status-warning", "status-danger");

  if (totalCredits >= 15 && totalCredits <= 18) {
    creditStatus.textContent = "Within recommended range";
    creditStatus.classList.add("status-good");
    return;
  }

  if (totalCredits < 15) {
    creditStatus.textContent = "Below recommended range";
    creditStatus.classList.add("status-warning");
    return;
  }

  creditStatus.textContent = "Above recommended range";
  creditStatus.classList.add("status-danger");
}

function parseStartEndHours(timeRange) {
  const parts = String(timeRange || "").split("-").map((part) => part.trim());
  if (parts.length !== 2) {
    return null;
  }

  const startHour = Number(parts[0].split(":")[0]);
  const endHour = Number(parts[1].split(":")[0]);

  if (!Number.isFinite(startHour) || !Number.isFinite(endHour) || startHour >= endHour) {
    return null;
  }

  return { startHour, endHour };
}

function renderPlanPager() {
  planPager.innerHTML = "";

  for (let index = 0; index < plans.length; index += 1) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "plan-page-btn";
    button.dataset.planIndex = String(index);
    button.textContent = String(index + 1);

    if (index === currentPlanIndex) {
      button.classList.add("active");
      button.setAttribute("aria-current", "page");
    }

    planPager.appendChild(button);
  }

  prevPlanBtn.disabled = currentPlanIndex <= 0;
  nextPlanBtn.disabled = currentPlanIndex >= plans.length - 1;
}

function isEntryInCurrentPlan(sectionId) {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return false;
  }

  return (currentPlan.entries || []).some((entry) => Number(entry.sectionId) === Number(sectionId));
}

function getSectionState(section) {
  if (isEntryInCurrentPlan(section.sectionId)) {
    const currentPlan = getCurrentPlan();
    const existing = (currentPlan.entries || []).find((entry) => Number(entry.sectionId) === Number(section.sectionId));
    return existing && existing.entryType === "WAITLIST" ? "waitlisted" : "added";
  }

  if (section.availability === "waitlist") {
    return "waitlist";
  }

  if (section.availability === "full") {
    return "full";
  }

  return "open";
}

function buildActionButton(section) {
  const state = getSectionState(section);

  if (state === "added") {
    return '<button type="button" class="small-btn add-course-btn added" disabled>Added</button>';
  }

  if (state === "waitlisted") {
    return '<button type="button" class="small-btn course-action-btn waitlisted" disabled>Waitlisted</button>';
  }

  if (state === "waitlist") {
    return `<button type="button" class="small-btn course-action-btn waitlist" data-section-id="${section.sectionId}">Add to Waitlist</button>`;
  }

  if (state === "full") {
    return '<button type="button" class="small-btn course-action-btn full" disabled>Full</button>';
  }

  return `<button type="button" class="small-btn add-course-btn" data-section-id="${section.sectionId}">Add</button>`;
}

function renderAvailableCourses() {
  const query = String(availableCourseSearch ? availableCourseSearch.value : "").trim().toLowerCase();

  const rows = availableSections.filter((section) => {
    if (!query) {
      return true;
    }

    const searchable = `${section.code} ${section.title} ${section.day} ${section.time} ${section.location} ${section.category}`.toLowerCase();
    return searchable.includes(query);
  });

  if (!rows.length) {
    availableCoursesBody.innerHTML = '<tr class="empty-row"><td colspan="8">No available courses found.</td></tr>';
    return;
  }

  availableCoursesBody.innerHTML = rows
    .map((section) => `
      <tr data-section-id="${section.sectionId}">
        <td>${section.code}</td>
        <td>${section.title}</td>
        <td><span class="pill ${section.category === "Lab" ? "pill-lab" : section.category === "Elective" ? "pill-elective" : "pill-core"}">${section.category}</span></td>
        <td data-credits="${section.credits}">${section.credits}</td>
        <td>${section.day}</td>
        <td>${section.time}</td>
        <td>${section.location}</td>
        <td>${buildActionButton(section)}</td>
      </tr>
    `)
    .join("");
}

function renderCurrentPlanTables() {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    selectedPlanBody.innerHTML = '<tr class="empty-row"><td colspan="5">No courses selected yet.</td></tr>';
    waitlistBody.innerHTML = '<tr class="empty-row"><td colspan="4">No waitlisted courses yet.</td></tr>';
    updateCreditStatus(0);
    return;
  }

  const selectedEntries = (currentPlan.entries || []).filter((entry) => entry.entryType === "SELECTED");
  const waitlistEntries = (currentPlan.entries || []).filter((entry) => entry.entryType === "WAITLIST");

  if (!selectedEntries.length) {
    selectedPlanBody.innerHTML = '<tr class="empty-row"><td colspan="5">No courses selected yet.</td></tr>';
  } else {
    selectedPlanBody.innerHTML = selectedEntries
      .map((entry) => `
        <tr data-entry-id="${entry.entryId}" data-section-id="${entry.sectionId}" data-code="${asText(entry.courseCode)}" data-credits="${Number(entry.credits || 0)}" data-day="${asText(entry.day)}" data-time="${toTimeRange(asText(entry.startTime, "00:00"), asText(entry.endTime, "00:00"))}" data-category="${categoryFromType(entry.sectionType)}" data-location="${asText(entry.venue, "TBA")}">
          <td>${asText(entry.courseCode)}</td>
          <td>${Number(entry.credits || 0)}</td>
          <td>${asText(entry.day)}</td>
          <td>${toTimeRange(asText(entry.startTime, "00:00"), asText(entry.endTime, "00:00"))}</td>
          <td>
            <div class="inline-actions">
              <button type="button" class="small-danger remove-course-btn" data-entry-id="${entry.entryId}">Remove</button>
            </div>
          </td>
        </tr>
      `)
      .join("");
  }

  if (!waitlistEntries.length) {
    waitlistBody.innerHTML = '<tr class="empty-row"><td colspan="4">No waitlisted courses yet.</td></tr>';
  } else {
    waitlistBody.innerHTML = waitlistEntries
      .map((entry) => `
        <tr data-entry-id="${entry.entryId}" data-section-id="${entry.sectionId}" data-code="${asText(entry.courseCode)}" data-day="${asText(entry.day)}" data-time="${toTimeRange(asText(entry.startTime, "00:00"), asText(entry.endTime, "00:00"))}">
          <td>${asText(entry.courseCode)}</td>
          <td>${asText(entry.day)}</td>
          <td>${toTimeRange(asText(entry.startTime, "00:00"), asText(entry.endTime, "00:00"))}</td>
          <td>
            <div class="inline-actions">
              <button type="button" class="small-danger remove-waitlist-btn" data-entry-id="${entry.entryId}">Remove</button>
            </div>
          </td>
        </tr>
      `)
      .join("");
  }

  const totalCredits = selectedEntries.reduce((sum, entry) => sum + Number(entry.credits || 0), 0);
  updateCreditStatus(totalCredits);
}

function renderPreviewTimetable() {
  previewTimetableBody.innerHTML = "";
  conflictList.innerHTML = "";

  const selectedRows = Array.from(selectedPlanBody.querySelectorAll("tr[data-code]"));

  const slotMap = {};
  const occupiedCells = new Set();
  const conflicts = [];

  selectedRows.forEach((row) => {
    const code = row.dataset.code;
    const category = row.dataset.category || "N/A";
    const location = row.dataset.location || "TBA";
    const day = row.dataset.day;
    const parsed = parseStartEndHours(row.dataset.time || "");

    if (!code || !day || !previewDays.includes(day) || !parsed) {
      return;
    }

    for (let hour = parsed.startHour; hour < parsed.endHour; hour += 1) {
      if (hour < previewStartHour || hour >= previewEndHour) {
        continue;
      }
      const key = `${day}|${hour}`;
      slotMap[key] = slotMap[key] || [];
      slotMap[key].push({ code, category, location });
    }
  });

  for (let hour = previewStartHour; hour < previewEndHour; hour += 1) {
    const row = document.createElement("tr");

    const timeCell = document.createElement("td");
    timeCell.className = "preview-time-col";
    timeCell.textContent = `${String(hour).padStart(2, "0")}:00 - ${String(hour + 1).padStart(2, "0")}:00`;
    row.appendChild(timeCell);

    previewDays.forEach((day) => {
      const occupiedKey = `${day}|${hour}`;
      if (occupiedCells.has(occupiedKey)) {
        return;
      }

      const cell = document.createElement("td");
      const key = `${day}|${hour}`;
      const entries = slotMap[key] || [];

      if (!entries.length) {
        cell.classList.add("preview-empty");
      } else if (entries.length > 1) {
        cell.classList.add("conflict-cell");
        conflicts.push(`${day} ${String(hour).padStart(2, "0")}:00 - ${String(hour + 1).padStart(2, "0")}:00: ${entries.map((entry) => entry.code).join(", ")}`);

        entries.forEach((entry) => {
          const block = document.createElement("div");
          block.className = "preview-course-block conflict-block";
          block.innerHTML = `<span class="preview-course-code">${entry.code}</span><span class="preview-course-meta">${entry.category} | ${entry.location}</span>`;
          cell.appendChild(block);
        });
      } else {
        const entry = entries[0];
        const block = document.createElement("div");
        block.className = "preview-course-block";
        block.innerHTML = `<span class="preview-course-code">${entry.code}</span><span class="preview-course-meta">${entry.category} | ${entry.location}</span>`;
        cell.appendChild(block);
      }

      row.appendChild(cell);
    });

    previewTimetableBody.appendChild(row);
  }

  if (conflicts.length) {
    conflictPanel.style.display = "block";
    noConflictPanel.style.display = "none";
    conflicts.forEach((conflict) => {
      const li = document.createElement("li");
      li.textContent = conflict;
      conflictList.appendChild(li);
    });
  } else {
    conflictPanel.style.display = "none";
    noConflictPanel.style.display = "block";
  }
}

function renderCurrentPlan() {
  renderPlanPager();
  renderCurrentPlanTables();
  renderAvailableCourses();
  renderPreviewTimetable();
}

async function loadCurrentStudent() {
  const me = await apiRequest("/api/session/me", { method: "GET" });
  if (!me || me.role !== "STUDENT" || !me.studentId) {
    throw new Error("Manage Plan requires a STUDENT login.");
  }
  currentStudentId = me.studentId;
  renderStudentSummary(me);
  return me;
}

async function loadAvailableSections() {
  const courses = await apiRequest("/api/sections", { method: "GET" });

  const flattened = [];
  (Array.isArray(courses) ? courses : []).forEach((course) => {
    const sections = Array.isArray(course.sections) ? course.sections : [];

    sections.forEach((section) => {
      const day = dayFromDateTime(section.startTime);
      const startTime = timeFromDateTime(section.startTime);
      const endTime = timeFromDateTime(section.endTime);
      const time = toTimeRange(startTime, endTime);

      let availability = "open";
      if (Number(section.availableEnroll || 0) <= 0) {
        availability = Number(section.availableWaitlist || 0) > 0 ? "waitlist" : "full";
      }

      flattened.push({
        sectionId: section.sectionId,
        code: asText(course.courseCode),
        title: asText(course.title),
        credits: Number(course.credits || 0),
        day,
        startTime,
        endTime,
        time,
        location: asText(section.venue, "TBA"),
        category: categoryFromType(section.type),
        availability
      });
    });
  });

  availableSections = flattened.sort((a, b) => `${a.code}-${a.sectionId}`.localeCompare(`${b.code}-${b.sectionId}`));
}

async function loadPlans() {
  const payload = await apiRequest(`/api/plans/${currentStudentId}`, { method: "GET" });
  plans = (Array.isArray(payload) ? payload : []).map((plan, index) => ({
    name: `Plan ${index + 1}`,
    planId: plan.planId,
    entries: Array.isArray(plan.entries) ? plan.entries : [],
    savedSignature: ""
  }));

  if (!plans.length) {
    const created = await apiRequest(`/api/plans/${currentStudentId}`, {
      method: "POST",
      body: JSON.stringify({})
    });

    plans = [{
      name: "Plan 1",
      planId: created.planId,
      entries: Array.isArray(created.entries) ? created.entries : [],
      savedSignature: ""
    }];
  }

  plans.forEach((plan) => {
    plan.savedSignature = getPlanSignature(plan);
  });
  currentPlanIndex = Math.min(currentPlanIndex, plans.length - 1);
}

function hasTimeOverlap(firstRange, secondRange) {
  return firstRange.startHour < secondRange.endHour && secondRange.startHour < firstRange.endHour;
}

async function removeEntryById(entryId) {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return;
  }

  await apiRequest(`/api/plans/${currentPlan.planId}/entries/${entryId}`, { method: "DELETE" });
  currentPlan.entries = (currentPlan.entries || []).filter((entry) => Number(entry.entryId) !== Number(entryId));
  await loadAvailableSections();
}

async function removeOverlaps(day, timeRange) {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return;
  }

  const newRange = parseStartEndHours(timeRange);
  if (!newRange) {
    return;
  }

  const overlaps = (currentPlan.entries || []).filter((entry) => {
    if (entry.day !== day) {
      return false;
    }
    const existingRange = parseStartEndHours(toTimeRange(entry.startTime, entry.endTime));
    if (!existingRange) {
      return false;
    }
    return hasTimeOverlap(newRange, existingRange);
  });

  if (!overlaps.length) {
    return;
  }

  const summary = overlaps.map((entry) => `${entry.courseCode} (${entry.startTime}-${entry.endTime})`).join(", ");
  const shouldReplace = window.confirm(`Selected section overlaps with ${summary}. Replace overlapping entries?`);
  if (!shouldReplace) {
    throw new Error("Operation cancelled by user.");
  }

  for (const overlap of overlaps) {
    await removeEntryById(overlap.entryId);
  }
}

async function addSectionToCurrentPlan(sectionId) {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return;
  }

  const section = availableSections.find((item) => Number(item.sectionId) === Number(sectionId));
  if (!section) {
    throw new Error("Section not found.");
  }

  if (isEntryInCurrentPlan(section.sectionId)) {
    return;
  }

  await removeOverlaps(section.day, section.time);

  const entryType = section.availability === "waitlist" ? "WAITLIST" : "SELECTED";
  const created = await apiRequest(`/api/plans/${currentPlan.planId}/entries`, {
    method: "POST",
    body: JSON.stringify({
      sectionId: section.sectionId,
      entryType,
      joinWaitlistOnAddFailure: entryType === "WAITLIST"
    })
  });

  const mapped = Object.assign({}, created, {
    sectionId: created.sectionId || section.sectionId,
    courseCode: created.courseCode || section.code,
    title: created.title || section.title,
    credits: Number(created.credits || section.credits),
    day: created.day || section.day,
    startTime: created.startTime || section.startTime,
    endTime: created.endTime || section.endTime,
    venue: created.venue || section.location,
    sectionType: created.sectionType || section.category
  });

  currentPlan.entries = currentPlan.entries || [];
  currentPlan.entries.push(mapped);
  await loadAvailableSections();
}

async function removeCurrentPlan() {
  if (plans.length <= 1) {
    window.alert("You must have at least one plan.");
    return;
  }

  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return;
  }

  const confirmed = window.confirm(`Remove ${currentPlan.name}? This cannot be undone.`);
  if (!confirmed) {
    return;
  }

  await apiRequest(`/api/plans/${currentPlan.planId}`, { method: "DELETE" });
  plans.splice(currentPlanIndex, 1);
  currentPlanIndex = Math.max(0, Math.min(currentPlanIndex, plans.length - 1));
  await loadAvailableSections();
  renderCurrentPlan();
}

async function addNewPlan() {
  const created = await apiRequest(`/api/plans/${currentStudentId}`, {
    method: "POST",
    body: JSON.stringify({})
  });

  plans.push({
    name: `Plan ${plans.length + 1}`,
    planId: created.planId,
    entries: Array.isArray(created.entries) ? created.entries : [],
    savedSignature: getPlanSignature(created)
  });

  currentPlanIndex = plans.length - 1;
  await loadAvailableSections();
  renderCurrentPlan();
}

availableCoursesBody.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-section-id]");
  if (!button || button.disabled) {
    return;
  }

  try {
    await addSectionToCurrentPlan(Number(button.dataset.sectionId));
    renderCurrentPlan();
  } catch (error) {
    if (error.message !== "Operation cancelled by user.") {
      window.alert(error.message || "Failed to add section.");
    }
  }
});

selectedPlanBody.addEventListener("click", async (event) => {
  const button = event.target.closest(".remove-course-btn");
  if (!button) {
    return;
  }

  try {
    await removeEntryById(Number(button.dataset.entryId));
    renderCurrentPlan();
  } catch (error) {
    window.alert(error.message || "Failed to remove course.");
  }
});

waitlistBody.addEventListener("click", async (event) => {
  const button = event.target.closest(".remove-waitlist-btn");
  if (!button) {
    return;
  }

  try {
    await removeEntryById(Number(button.dataset.entryId));
    renderCurrentPlan();
  } catch (error) {
    window.alert(error.message || "Failed to remove waitlisted course.");
  }
});

submitPlanBtn.addEventListener("click", () => {
  const currentPlan = getCurrentPlan();
  if (!currentPlan) {
    return;
  }

  currentPlan.savedSignature = getPlanSignature(currentPlan);
  window.alert("Plan changes saved.");
});

undoPlanBtn.addEventListener("click", async () => {
  if (!hasUnsavedPlanChanges()) {
    return;
  }

  const confirmed = window.confirm("Undo unsaved changes and reload plan from server?");
  if (!confirmed) {
    return;
  }

  try {
    await loadPlans();
    renderCurrentPlan();
  } catch (error) {
    window.alert(error.message || "Failed to reload plans.");
  }
});

prevPlanBtn.addEventListener("click", () => {
  if (currentPlanIndex > 0) {
    currentPlanIndex -= 1;
    renderCurrentPlan();
  }
});

nextPlanBtn.addEventListener("click", () => {
  if (currentPlanIndex < plans.length - 1) {
    currentPlanIndex += 1;
    renderCurrentPlan();
  }
});

addPlanBtn.addEventListener("click", async () => {
  try {
    await addNewPlan();
  } catch (error) {
    window.alert(error.message || "Failed to create plan.");
  }
});

removePlanBtn.addEventListener("click", async () => {
  try {
    await removeCurrentPlan();
  } catch (error) {
    window.alert(error.message || "Failed to remove plan.");
  }
});

planPager.addEventListener("click", (event) => {
  const button = event.target.closest(".plan-page-btn");
  if (!button) {
    return;
  }

  const target = Number(button.dataset.planIndex);
  if (!Number.isInteger(target) || target === currentPlanIndex) {
    return;
  }

  currentPlanIndex = target;
  renderCurrentPlan();
});

if (availableCourseSearch) {
  availableCourseSearch.addEventListener("input", () => {
    renderAvailableCourses();
  });
}

if (academicTermSelect) {
  academicTermSelect.addEventListener("change", () => {
    if (summaryAcademicTerm) {
      const selectedTerm = academicTermSelect.selectedOptions.length
        ? academicTermSelect.selectedOptions[0].textContent
        : "Current Term";
      summaryAcademicTerm.textContent = asText(selectedTerm, "Current Term");
    }
  });
}

window.addEventListener("beforeunload", (event) => {
  if (!hasUnsavedPlanChanges()) {
    return;
  }

  event.preventDefault();
  event.returnValue = "";
});

(async function initPage() {
  try {
    await loadCurrentStudent();
    await loadAvailableSections();
    await loadPlans();
    renderCurrentPlan();
  } catch (error) {
    window.alert("Failed to initialize Manage Plan: " + (error.message || "Unknown error"));
  }
})();
