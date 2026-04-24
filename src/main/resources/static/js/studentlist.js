const courseList = document.getElementById("courseList");
const totalCourses = document.getElementById("totalCourses");
const totalEnrollments = document.getElementById("totalEnrollments");
const totalInstructors = document.getElementById("totalInstructors");

let sectionGroups = [];

function statusClass(status) {
  if (status === "Active") return "status-pill status-active";
  if (status === "Pending") return "status-pill status-pending";
  return "status-pill status-onleave";
}

function asText(value, fallback = "-") {
  if (value === null || value === undefined) {
    return fallback;
  }

  const text = String(value).trim();
  return text.length === 0 ? fallback : text;
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

function buildStudentTableRows(students) {
  if (!students || students.length === 0) {
    return `
      <tr>
        <td colspan="6">No enrolled students for this section.</td>
      </tr>
    `;
  }

  return students
    .map(
      (student) => `
        <tr>
          <td>${asText(student.id)}</td>
          <td>${asText(student.name)}</td>
          <td>${asText(student.programme)}</td>
          <td>${asText(student.year)}</td>
          <td>${asText(student.email)}</td>
          <td><span class="${statusClass(asText(student.status))}">${asText(student.status)}</span></td>
        </tr>
      `
    )
    .join("");
}

function buildCourseTable(group) {
  return `
    <div class="table-wrapper">
      <table aria-label="Student list for ${asText(group.code)} section ${asText(group.sectionId)}">
        <thead>
          <tr>
            <th>Student ID</th>
            <th>Name</th>
            <th>Programme</th>
            <th>Year</th>
            <th>Email</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          ${buildStudentTableRows(group.students)}
        </tbody>
      </table>
    </div>
  `;
}

function renderSummary(groups) {
  const enrollmentCount = groups.reduce((sum, group) => sum + (Array.isArray(group.students) ? group.students.length : 0), 0);
  const instructorCount = new Set(groups.map((group) => asText(group.teacher))).size;

  totalCourses.textContent = `${groups.length} Sections`;
  totalEnrollments.textContent = `${enrollmentCount} Students`;
  totalInstructors.textContent = `${instructorCount} Teachers`;
}

function renderCoursePanels(groups) {
  courseList.innerHTML = "";

  if (!groups || groups.length === 0) {
    courseList.innerHTML = '<p class="muted">No sections available for the current account.</p>';
    return;
  }

  groups.forEach((group, index) => {
    const panel = document.createElement("details");
    panel.className = "course-panel";
    panel.dataset.index = String(index);
    panel.innerHTML = `
      <summary>
        <div class="course-meta">
          <span class="course-title">${asText(group.code)} - ${asText(group.title)} (Section ${asText(group.sectionId)})</span>
          <span class="course-subtitle">${asText(group.teacher)} | ${asText(group.day)} | ${asText(group.time)}</span>
        </div>
        <div class="course-summary-right">
          <span class="course-count">${Array.isArray(group.students) ? group.students.length : 0} Students</span>
          <span class="expand-icon" aria-hidden="true">&#9660;</span>
        </div>
      </summary>
      <div class="course-content">
        <div class="course-actions">
          <button type="button" class="export-course-btn" data-index="${index}">Export ${asText(group.code)} List</button>
        </div>
        ${buildCourseTable(group)}
      </div>
    `;

    courseList.appendChild(panel);
  });

  addCoursePanelAnimations();
}

function addCoursePanelAnimations() {
  const panels = document.querySelectorAll(".course-panel");

  panels.forEach((panel) => {
    const summary = panel.querySelector("summary");
    const content = panel.querySelector(".course-content");

    let animation = null;
    let isClosing = false;

    summary.addEventListener("click", function (event) {
      event.preventDefault();

      const startHeight = `${panel.offsetHeight}px`;
      const isOpen = panel.open;

      panel.style.overflow = "hidden";

      if (animation) {
        animation.cancel();
      }

      if (isOpen) {
        isClosing = true;
        const endHeight = `${summary.offsetHeight}px`;
        animation = panel.animate(
          {
            height: [startHeight, endHeight]
          },
          {
            duration: 260,
            easing: "ease"
          }
        );
      } else {
        panel.open = true;
        const endHeight = `${summary.offsetHeight + content.offsetHeight}px`;
        animation = panel.animate(
          {
            height: [startHeight, endHeight]
          },
          {
            duration: 260,
            easing: "ease"
          }
        );
      }

      animation.onfinish = function () {
        panel.open = !isClosing;
        animation = null;
        isClosing = false;
        panel.style.height = "";
        panel.style.overflow = "";
      };

      animation.oncancel = function () {
        animation = null;
        isClosing = false;
      };
    });
  });
}

function exportCourseList(group) {
  const exportWindow = window.open("", "_blank");
  if (!exportWindow) {
    window.alert("Unable to open export window. Please allow pop-ups and try again.");
    return;
  }

  const courseTable = buildCourseTable(group);

  exportWindow.document.write(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <title>Exported ${asText(group.code)} Student List</title>
      <link rel="stylesheet" href="/css/print-studentlist.css" />
      <script src="/js/export-controls.js" defer></script>
    </head>
    <body>
      <div class="print-controls">
        <button id="printBtn" type="button">Print Student List</button>
        <button id="closeBtn" type="button">Close</button>
      </div>

      <h1>Export Student List</h1>
      <p>Generated from Check Student List</p>

      <div class="meta">
        <div><strong>Course:</strong> ${asText(group.code)} ${asText(group.title)}</div>
        <div><strong>Section:</strong> ${asText(group.sectionId)}</div>
        <div><strong>Teacher:</strong> ${asText(group.teacher)}</div>
        <div><strong>Class Time:</strong> ${asText(group.day)} ${asText(group.time)}</div>
        <div><strong>Total Students:</strong> ${Array.isArray(group.students) ? group.students.length : 0}</div>
      </div>

      <h2>${asText(group.code)} Student List</h2>
      ${courseTable}

      <div class="footer">© 2026 University Course Registration System</div>
    </body>
    </html>
  `);

  exportWindow.document.close();
}

courseList.addEventListener("click", function (event) {
  const button = event.target.closest(".export-course-btn");
  if (!button) {
    return;
  }

  const index = Number(button.dataset.index);
  const group = sectionGroups[index];
  if (!group) {
    return;
  }

  exportCourseList(group);
});

async function initPage() {
  courseList.innerHTML = '<p class="muted">Loading student list...</p>';

  try {
    const groups = await apiRequest("/api/studentlist", { method: "GET" });
    sectionGroups = Array.isArray(groups) ? groups : [];
    renderSummary(sectionGroups);
    renderCoursePanels(sectionGroups);
  } catch (error) {
    courseList.innerHTML = `<p class="muted">Failed to load student list: ${asText(error.message)}</p>`;
  }
}

initPage();
