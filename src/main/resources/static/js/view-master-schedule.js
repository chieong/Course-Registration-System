const scheduleForm = document.querySelector(".schedule-form");
const scheduleBody = document.getElementById("masterScheduleBody");
const statusText = document.getElementById("masterScheduleStatus");

const filters = {
  courseCode: document.getElementById("courseCode"),
  department: document.getElementById("department"),
  instructor: document.getElementById("instructor"),
  day: document.getElementById("day"),
  time: document.getElementById("time"),
  location: document.getElementById("location")
};

let coursesData = [];

const DAY_NAMES = {
  M: "Monday",
  T: "Tuesday",
  W: "Wednesday",
  R: "Thursday",
  F: "Friday",
  S: "Saturday"
};

function asText(value, fallback = "-") {
  if (value === null || value === undefined) {
    return fallback;
  }
  const str = String(value).trim();
  return str.length === 0 ? fallback : str;
}

function toDayCode(startTime) {
  if (!startTime || startTime.length < 10) {
    return "-";
  }

  const date = new Date(startTime.replace(" ", "T"));
  if (Number.isNaN(date.getTime())) {
    return "-";
  }

  const day = date.getDay();
  if (day === 1) return "M";
  if (day === 2) return "T";
  if (day === 3) return "W";
  if (day === 4) return "R";
  if (day === 5) return "F";
  if (day === 6) return "S";
  return "-";
}

function formatTimeRange(startTime, endTime) {
  const start = asText(startTime);
  const end = asText(endTime);
  if (start === "-" && end === "-") {
    return "-";
  }
  return `${start} - ${end}`;
}

function normalizeRows(courses) {
  return courses.flatMap((course) => {
    const sections = Array.isArray(course.sections) ? course.sections : [];

    if (sections.length === 0) {
      return [
        {
          courseCode: asText(course.courseCode),
          title: asText(course.title),
          credits: asText(course.credits),
          sectionId: "-",
          type: "-",
          dayCode: "-",
          dayLabel: "-",
          startTime: "-",
          endTime: "-",
          venue: "-",
          instructor: "-",
          enrollment: "-",
          waitlist: "-",
          rules: buildRules(course)
        }
      ];
    }

    return sections.map((section) => {
      const dayCode = toDayCode(section.startTime);
      return {
        courseCode: asText(course.courseCode),
        title: asText(course.title),
        credits: asText(course.credits),
        sectionId: asText(section.sectionId),
        type: asText(section.type).toUpperCase(),
        dayCode,
        dayLabel: DAY_NAMES[dayCode] || "-",
        startTime: asText(section.startTime),
        endTime: asText(section.endTime),
        venue: asText(section.venue),
        instructor: Array.isArray(section.instructors) && section.instructors.length > 0
          ? section.instructors.join(", ")
          : "-",
        enrollment: `${section.enrolled}/${section.enrollCapacity} (Available: ${section.availableEnroll})`,
        waitlist: `${section.waitlisted}/${section.waitlistCapacity} (Available: ${section.availableWaitlist})`,
        rules: buildRules(course)
      };
    });
  });
}

function buildRules(course) {
  const prereq = Array.isArray(course.prerequisites) && course.prerequisites.length > 0
    ? `Prereq: ${course.prerequisites.join(", ")}`
    : "Prereq: -";

  const exclusive = Array.isArray(course.exclusives) && course.exclusives.length > 0
    ? `Exclusive: ${course.exclusives.join(", ")}`
    : "Exclusive: -";

  return `${prereq} | ${exclusive}`;
}

function matchesFilters(row) {
  const courseCode = filters.courseCode.value.trim().toLowerCase();
  const instructor = filters.instructor.value.trim().toLowerCase();
  const location = filters.location.value.trim().toLowerCase();
  const department = filters.department.value.trim().toLowerCase();
  const selectedDay = filters.day.value;
  const selectedTime = filters.time.value;

  const byCourse = !courseCode || row.courseCode.toLowerCase().includes(courseCode);
  const byInstructor = !instructor || row.instructor.toLowerCase().includes(instructor);
  const byLocation = !location || row.venue.toLowerCase().includes(location);
  const byDepartment = !department || row.courseCode.toLowerCase().startsWith(department);
  const byDay = !selectedDay || row.dayLabel.toLowerCase() === selectedDay;
  const byTime = !selectedTime || matchesTimeRange(row.startTime, selectedTime);

  return byCourse && byInstructor && byLocation && byDepartment && byDay && byTime;
}

function matchesTimeRange(startTime, selectedTime) {
  const hour = Number.parseInt(String(startTime).slice(11, 13), 10);
  if (Number.isNaN(hour)) {
    return false;
  }

  if (selectedTime === "morning") {
    return hour < 12;
  }
  if (selectedTime === "afternoon") {
    return hour >= 12 && hour < 18;
  }
  return hour >= 18;
}

function renderTable(rows) {
  scheduleBody.innerHTML = "";

  if (rows.length === 0) {
    const emptyRow = document.createElement("tr");
    emptyRow.innerHTML = '<td colspan="12">No courses match the current filters.</td>';
    scheduleBody.appendChild(emptyRow);
    statusText.textContent = "No course sections found for your current filters.";
    return;
  }

  for (const row of rows) {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${row.courseCode}</td>
      <td>${row.title}</td>
      <td>${row.credits}</td>
      <td>${row.sectionId}</td>
      <td>${row.type}</td>
      <td>${row.dayLabel}</td>
      <td>${formatTimeRange(row.startTime, row.endTime)}</td>
      <td>${row.venue}</td>
      <td>${row.instructor}</td>
      <td>${row.enrollment}</td>
      <td>${row.waitlist}</td>
      <td>${row.rules}</td>
    `;
    scheduleBody.appendChild(tr);
  }

  statusText.textContent = `Showing ${rows.length} section row(s).`;
}

function applyFilters() {
  const rows = normalizeRows(coursesData).filter(matchesFilters);
  renderTable(rows);
}

async function loadSchedule() {
  statusText.textContent = "Loading master schedule...";

  try {
    const response = await fetch("/api/sections");
    if (!response.ok) {
      throw new Error(`Failed to fetch schedule: ${response.status}`);
    }

    coursesData = await response.json();
    applyFilters();
  } catch (error) {
    scheduleBody.innerHTML = '<tr><td colspan="12">Unable to load master schedule data.</td></tr>';
    statusText.textContent = error.message;
  }
}

scheduleForm.addEventListener("submit", (event) => {
  event.preventDefault();
  applyFilters();
});

document.addEventListener("DOMContentLoaded", () => {
  loadSchedule();
});
