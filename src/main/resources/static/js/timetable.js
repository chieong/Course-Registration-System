const START = 9;
const END = 21;
const SLOT_HEIGHT = 160;
const DAY_SEQUENCE = ["M", "T", "W", "R", "F", "S"];
const DAY_NAME = {
  M: "Monday",
  T: "Tuesday",
  W: "Wednesday",
  R: "Thursday",
  F: "Friday",
  S: "Saturday"
};

let classes = [];
let currentUser = null;

const ttBody = document.getElementById("tt-body");
const detailsBody = document.getElementById("detailsBody");
const statusLabel = document.getElementById("timetableStatus");
const summaryName = document.getElementById("summaryName");
const summaryProgramme = document.getElementById("summaryProgramme");
const summaryTotalCourses = document.getElementById("summaryTotalCourses");

function timeToHour(time) {
  return Number.parseInt(String(time).slice(0, 2), 10);
}

function asText(value, fallback = "-") {
  if (value === null || value === undefined) {
    return fallback;
  }

  const text = String(value).trim();
  return text.length === 0 ? fallback : text;
}

function formatType(type) {
  const cleaned = asText(type, "-").toLowerCase();
  if (cleaned === "-") {
    return "-";
  }
  return cleaned.charAt(0).toUpperCase() + cleaned.slice(1);
}

function formatDay(dayCode) {
  return DAY_NAME[dayCode] || "-";
}

function renderGrid() {
  ttBody.innerHTML = "";

  for (let hour = START; hour <= END; hour++) {
    const row = document.createElement("tr");
    const timeCell = document.createElement("td");
    timeCell.className = "time-col";
    timeCell.textContent = String(hour).padStart(2, "0") + ":00";
    row.appendChild(timeCell);

    for (const day of DAY_SEQUENCE) {
      const cell = document.createElement("td");
      cell.className = "empty-slot";
      cell.dataset.day = day;
      cell.dataset.hour = hour;
      row.appendChild(cell);
    }

    ttBody.appendChild(row);
  }
}

function placeClass(course) {
  const startHour = timeToHour(course.start);
  const endHour = timeToHour(course.end);
  const duration = endHour - startHour;

  if (duration <= 0) {
    return;
  }

  const startCell = ttBody.querySelector(`td[data-day="${course.day}"][data-hour="${startHour}"]`);
  if (!startCell) {
    return;
  }

  startCell.className = "";
  startCell.rowSpan = duration;
  startCell.style.height = `${duration * SLOT_HEIGHT}px`;

  for (let hour = startHour + 1; hour < endHour; hour++) {
    const coveredCell = ttBody.querySelector(`td[data-day="${course.day}"][data-hour="${hour}"]`);
    if (coveredCell) {
      coveredCell.remove();
    }
  }

  const block = document.createElement("div");
  block.className = "class-block";
  block.innerHTML = `
    <span class="course-code-link">${course.code}</span>
    <div class="course-title">${course.title}</div>
    <div class="venue">${course.venue}</div>
    <span class="section-tag">${formatType(course.type)}</span>
  `;

  startCell.appendChild(block);
}

function renderDetailsTable(items) {
  detailsBody.innerHTML = "";

  if (items.length === 0) {
    const emptyRow = document.createElement("tr");
    emptyRow.innerHTML = '<td colspan="7">No timetable records found.</td>';
    detailsBody.appendChild(emptyRow);
    return;
  }

  for (const item of items) {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${asText(item.code)}</td>
      <td>${asText(item.title)}</td>
      <td>${formatDay(item.day)}</td>
      <td>${asText(item.start)} - ${asText(item.end)}</td>
      <td>${asText(item.venue)}</td>
      <td>${asText(item.lecturer)}</td>
      <td>${formatType(item.type)}</td>
    `;
    detailsBody.appendChild(row);
  }
}

function renderAll() {
  renderGrid();
  classes.forEach(placeClass);
  renderDetailsTable(classes);
}

function updateSummary(session, timetable) {
  summaryName.textContent = asText(timetable.displayName || (session ? session.displayName : ""));
  summaryProgramme.textContent = asText(timetable.programme || timetable.role);
  summaryTotalCourses.textContent = `${timetable.totalCourses} Course${timetable.totalCourses === 1 ? "" : "s"}`;
}

async function loadSession() {
  const response = await fetch("/api/session/me");
  if (!response.ok) {
    throw new Error("Unable to identify current user session.");
  }
  return response.json();
}

async function loadTimetable() {
  const response = await fetch("/api/timetable/me");
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Unable to load timetable.");
  }
  return response.json();
}

async function bootstrap() {
  statusLabel.textContent = "Loading timetable data...";

  try {
    const [session, timetable] = await Promise.all([loadSession(), loadTimetable()]);
    currentUser = session;

    classes = Array.isArray(timetable.entries) ? timetable.entries : [];
    updateSummary(session, timetable);
    renderAll();
    statusLabel.textContent = `Loaded ${classes.length} timetable record(s) for ${asText(timetable.role)}.`;
  } catch (error) {
    classes = [];
    renderAll();
    statusLabel.textContent = error.message;
  }
}

document.getElementById("exportBtn").addEventListener("click", function () {
  const weeklyTableClone = document.getElementById("weeklyTable").cloneNode(true);
  const detailsTableClone = document.getElementById("detailsTable").cloneNode(true);
  const weeklyTable = weeklyTableClone.outerHTML;
  const detailsTable = detailsTableClone.outerHTML;

  const exportWindow = window.open("", "_blank");
  if (!exportWindow) {
    alert("Unable to open export window. Please allow pop-ups and try again.");
    return;
  }

  const name = summaryName.textContent;
  const programme = summaryProgramme.textContent;
  const totalCourses = summaryTotalCourses.textContent;
  const roleLabel = currentUser && currentUser.role ? currentUser.role : "USER";

  exportWindow.document.open();
  exportWindow.document.write(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <title>Exported Timetable</title>
      <link rel="stylesheet" href="/css/print-timetable.css" />
      <script src="/js/export-controls.js" defer></script>
    </head>
    <body>
      <div class="print-controls">
        <button id="printBtn" type="button">Print Timetable</button>
        <button id="closeBtn" type="button">Close</button>
      </div>

      <h1>Export Timetable</h1>
      <p>Generated from Check Timetable</p>

      <div class="meta">
        <div><strong>Name:</strong> ${name}</div>
        <div><strong>Role:</strong> ${roleLabel}</div>
        <div><strong>Programme:</strong> ${programme}</div>
        <div><strong>Total Courses:</strong> ${totalCourses}</div>
      </div>

      <h2>Weekly Timetable</h2>
      ${weeklyTable}

      <h2>Timetable Details</h2>
      ${detailsTable}

      <div class="footer">© 2026 University Course Registration System</div>
    </body>
    </html>
  `);

  exportWindow.document.close();
});

bootstrap();
