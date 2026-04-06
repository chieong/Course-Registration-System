const START = 9;
const END = 22;
const SLOT_HEIGHT = 160;
const classes = [
  { code: "CSC204", title: "Database Systems", day: "M", start: "09:00", end: "12:00", venue: "Lab 2", lecturer: "Dr. Lim", type: "Lab" },
  { code: "CSC220", title: "Web Development", day: "T", start: "10:00", end: "11:00", venue: "Lab 3", lecturer: "Mr. Jonathan Wong", type: "Lab" },
  { code: "BUS150", title: "Entrepreneurship", day: "S", start: "10:00", end: "13:00", venue: "Room C2", lecturer: "Dr. Karen Ho", type: "Lecture" },
  { code: "CSC230", title: "Operating Systems", day: "F", start: "11:00", end: "13:00", venue: "Auditorium 1", lecturer: "Dr. Lim", type: "Lecture" },
  { code: "MAT210", title: "Discrete Mathematics", day: "M", start: "13:00", end: "15:00", venue: "Room A1", lecturer: "Prof. Sarah Lee", type: "Lecture" },
  { code: "ENG205", title: "Technical Communication", day: "R", start: "14:00", end: "16:00", venue: "Online", lecturer: "Prof. Daniel Ng", type: "Tutorial" },
  { code: "CSC201", title: "Data Structures", day: "W", start: "15:00", end: "16:00", venue: "Room B3", lecturer: "Dr. Aisha Rahman", type: "Lecture" }
];

const days = ["M", "T", "W", "R", "F", "S"];
const ttBody = document.getElementById("tt-body");

const timeToHour = (time) => parseInt(time.split(":")[0], 10);

function renderGrid() {
  ttBody.innerHTML = "";
  for (let hour = START; hour <= END; hour++) {
    const row = document.createElement("tr");
    const timeCell = document.createElement("td");
    timeCell.className = "time-col";
    timeCell.textContent = String(hour).padStart(2, "0") + ":00";
    row.appendChild(timeCell);

    for (const day of days) {
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
    <a href="/course/${course.code}" class="course-code-link">${course.code}</a>
    <div class="course-title">${course.title}</div>
    <div class="venue">${course.venue}</div>
    <span class="section-tag">${course.type}</span>
  `;

  startCell.appendChild(block);
}

renderGrid();
classes.forEach(placeClass);

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
        <div><strong>Name:</strong> Isaac Tan</div>
        <div><strong>Academic Term:</strong> Semester 2, 2026</div>
        <div><strong>Programme:</strong> Bachelor of Computer Science</div>
        <div><strong>Total Courses:</strong> ${classes.length} Courses</div>
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
