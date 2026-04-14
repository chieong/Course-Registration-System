const courseLists = [
  {
    code: "CSC204",
    title: "Database Systems",
    teacher: "Dr. Lim",
    day: "Monday",
    time: "09:00 - 12:00",
    students: [
      { id: "S2301001", name: "Isaac Tan", programme: "BSc Computer Science", year: "Year 2", email: "isaac.tan@cityu.edu.hk", status: "Active" },
      { id: "S2301024", name: "Chloe Wong", programme: "BSc Computer Science", year: "Year 2", email: "chloe.wong@cityu.edu.hk", status: "Active" },
      { id: "S2400561", name: "Mei Lin", programme: "BSc Computer Science", year: "Year 1", email: "mei.lin@cityu.edu.hk", status: "Pending" }
    ]
  },
  {
    code: "CSC230",
    title: "Operating Systems",
    teacher: "Dr. Lim",
    day: "Friday",
    time: "11:00 - 13:00",
    students: [
      { id: "S2200903", name: "Priya Sharma", programme: "BSc Computer Science", year: "Year 3", email: "priya.sharma@cityu.edu.hk", status: "Active" },
      { id: "S2100402", name: "Yuki Sato", programme: "BSc Computer Science", year: "Year 4", email: "yuki.sato@cityu.edu.hk", status: "Active" },
      { id: "S2201210", name: "Marcus Lee", programme: "BSc Computer Science", year: "Year 3", email: "marcus.lee@cityu.edu.hk", status: "On Leave" }
    ]
  },
  {
    code: "CSC220",
    title: "Web Development",
    teacher: "Mr. Jonathan Wong",
    day: "Tuesday",
    time: "10:00 - 11:00",
    students: [
      { id: "S2300877", name: "Fatima Noor", programme: "BSc Data Science", year: "Year 2", email: "fatima.noor@cityu.edu.hk", status: "Active" },
      { id: "S2300768", name: "Daniel Ng", programme: "BSc Data Science", year: "Year 2", email: "daniel.ng@cityu.edu.hk", status: "Pending" }
    ]
  },
  {
    code: "ENG205",
    title: "Technical Communication",
    teacher: "Prof. Daniel Ng",
    day: "Thursday",
    time: "14:00 - 16:00",
    students: [
      { id: "S2201143", name: "Aarav Kumar", programme: "BSc Computer Science", year: "Year 3", email: "aarav.kumar@cityu.edu.hk", status: "Active" },
      { id: "S2400329", name: "Kevin Ho", programme: "BSc Information Systems", year: "Year 1", email: "kevin.ho@cityu.edu.hk", status: "Pending" }
    ]
  }
];

const courseList = document.getElementById("courseList");
const totalCourses = document.getElementById("totalCourses");
const totalEnrollments = document.getElementById("totalEnrollments");
const totalInstructors = document.getElementById("totalInstructors");

function statusClass(status) {
  if (status === "Active") return "status-pill status-active";
  if (status === "Pending") return "status-pill status-pending";
  return "status-pill status-onleave";
}

function buildStudentTableRows(students) {
  return students
    .map(
      (student) => `
    <tr>
      <td>${student.id}</td>
      <td>${student.name}</td>
      <td>${student.programme}</td>
      <td>${student.year}</td>
      <td>${student.email}</td>
      <td><span class="${statusClass(student.status)}">${student.status}</span></td>
    </tr>
  `
    )
    .join("");
}

function buildCourseTable(course) {
  return `
    <div class="table-wrapper">
      <table aria-label="Student list for ${course.code}">
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
          ${buildStudentTableRows(course.students)}
        </tbody>
      </table>
    </div>
  `;
}

function renderSummary() {
  const enrollmentCount = courseLists.reduce((sum, course) => sum + course.students.length, 0);
  const instructorCount = new Set(courseLists.map((course) => course.teacher)).size;

  totalCourses.textContent = `${courseLists.length} Courses`;
  totalEnrollments.textContent = `${enrollmentCount} Enrollments`;
  totalInstructors.textContent = `${instructorCount} Teachers`;
}

function renderCoursePanels() {
  courseList.innerHTML = "";

  courseLists.forEach((course, index) => {
    const panel = document.createElement("details");
    panel.className = "course-panel";
    panel.dataset.index = index;
    panel.innerHTML = `
      <summary>
        <div class="course-meta">
          <span class="course-title">${course.code} - ${course.title}</span>
          <span class="course-subtitle">${course.teacher} | ${course.day} | ${course.time}</span>
        </div>
        <div class="course-summary-right">
          <span class="course-count">${course.students.length} Students</span>
          <span class="expand-icon" aria-hidden="true">&#9660;</span>
        </div>
      </summary>
      <div class="course-content">
        <div class="course-actions">
          <button type="button" class="export-course-btn" data-index="${index}">Export ${course.code} List</button>
        </div>
        ${buildCourseTable(course)}
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

function exportCourseList(course) {
  const exportWindow = window.open("", "_blank");
  if (!exportWindow) {
    alert("Unable to open export window. Please allow pop-ups and try again.");
    return;
  }

  const courseTable = buildCourseTable(course);

  exportWindow.document.write(`
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <title>Exported ${course.code} Student List</title>
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
        <div><strong>Academic Term:</strong> Semester 2, 2026</div>
        <div><strong>Course:</strong> ${course.code} ${course.title}</div>
        <div><strong>Teacher:</strong> ${course.teacher}</div>
        <div><strong>Class Time:</strong> ${course.day} ${course.time}</div>
        <div><strong>Total Students:</strong> ${course.students.length}</div>
      </div>

      <h2>${course.code} Student List</h2>
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
  const course = courseLists[index];
  if (!course) {
    return;
  }

  exportCourseList(course);
});

renderSummary();
renderCoursePanels();
