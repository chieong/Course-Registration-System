		const availableCoursesBody = document.getElementById("availableCoursesBody");
		const selectedPlanBody = document.getElementById("selectedPlanBody");
		const waitlistBody = document.getElementById("waitlistBody");
		const creditValue = document.getElementById("creditValue");
		const creditStatus = document.getElementById("creditStatus");
		const clearPlanBtn = document.getElementById("clearPlanBtn");
		const submitPlanBtn = document.getElementById("submitPlanBtn");
		const undoPlanBtn = document.getElementById("undoPlanBtn");
		const previewTimetableBody = document.getElementById("previewTimetableBody");
		const conflictPanel = document.getElementById("conflictPanel");
		const conflictList = document.getElementById("conflictList");
		const noConflictPanel = document.getElementById("noConflictPanel");
		const availableCourseSearch = document.getElementById("availableCourseSearch");

		const previewDays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
		const previewStartHour = 9;
		const previewEndHour = 22;
		let lastSavedPlanSignature = "";
		const initialSelectedHTML = selectedPlanBody.innerHTML;
		const initialWaitlistHTML = waitlistBody.innerHTML;

		function getSelectedRows() {
			return Array.from(selectedPlanBody.querySelectorAll("tr[data-code]"));
		}

		function getWaitlistedRows() {
			return Array.from(waitlistBody.querySelectorAll("tr[data-code]"));
		}

		function getCurrentPlanSignature() {
			const selectedCourses = getSelectedRows().map((row) => ({
				code: row.dataset.code,
				credits: row.dataset.credits,
				day: row.dataset.day,
				time: row.dataset.time,
				type: "selected"
			}));

			const waitlistedCourses = getWaitlistedRows().map((row) => ({
				code: row.dataset.code,
				day: row.dataset.day,
				time: row.dataset.time,
				type: "waitlisted"
			}));

			return JSON.stringify({
				selectedCourses,
				waitlistedCourses
			});
		}

		function hasUnsavedPlanChanges() {
			return getCurrentPlanSignature() !== lastSavedPlanSignature;
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

		function recalculateCredits() {
			const totalCredits = getSelectedRows().reduce((sum, row) => {
				return sum + Number(row.dataset.credits || 0);
			}, 0);

			updateCreditStatus(totalCredits);
		}

		function toggleEmptyState() {
			const emptyPlanRow = document.getElementById("emptyPlanRow");
			if (!emptyPlanRow) return;

			emptyPlanRow.style.display = getSelectedRows().length ? "none" : "table-row";
		}

		function toggleWaitlistEmptyState() {
			const emptyWaitlistRow = document.getElementById("emptyWaitlistRow");
			if (!emptyWaitlistRow) return;

			emptyWaitlistRow.style.display = getWaitlistedRows().length ? "none" : "table-row";
		}

		function setCourseButtonState(courseCode, state) {
			const btn = availableCoursesBody.querySelector(`button[data-code="${courseCode}"]`);
			if (!btn) return;

			btn.classList.remove("added", "waitlist", "waitlisted", "full", "add-course-btn", "course-action-btn");
			btn.dataset.state = state;

			if (state === "added") {
				btn.disabled = true;
				btn.classList.add("small-btn", "add-course-btn", "added");
				btn.textContent = "Added";
				return;
			}

			if (state === "waitlisted") {
				btn.disabled = true;
				btn.classList.add("small-btn", "course-action-btn", "waitlisted");
				btn.textContent = "Waitlisted";
				return;
			}

			if (state === "waitlist") {
				btn.disabled = false;
				btn.classList.add("small-btn", "course-action-btn", "waitlist");
				btn.textContent = "Add to Waitlist";
				return;
			}

			if (state === "full") {
				btn.disabled = true;
				btn.classList.add("small-btn", "course-action-btn", "full");
				btn.textContent = "Full";
				return;
			}

			btn.disabled = false;
			btn.classList.add("small-btn", "add-course-btn");
			btn.textContent = "Add";
		}

		function resetCourseButtonState(courseCode) {
			const row = availableCoursesBody.querySelector(`tr button[data-code="${courseCode}"]`)?.closest("tr");
			if (!row) return;

			const availability = row.dataset.availability || "open";
			setCourseButtonState(courseCode, availability);
		}

		function filterAvailableCourses() {
			const query = String(availableCourseSearch ? availableCourseSearch.value : "")
				.trim()
				.toLowerCase();

			Array.from(availableCoursesBody.querySelectorAll("tr")).forEach((row) => {
				const rowText = row.textContent.toLowerCase();
				row.style.display = !query || rowText.includes(query) ? "" : "none";
			});
		}

		function formatHour(hour) {
			return `${String(hour).padStart(2, "0")}:00`;
		}

		function parseStartEndHours(timeRange) {
			const [startTime, endTime] = String(timeRange).split("-").map((part) => part.trim());
			if (!startTime || !endTime) return null;

			const startHour = Number(startTime.split(":")[0]);
			const endHour = Number(endTime.split(":")[0]);

			if (!Number.isFinite(startHour) || !Number.isFinite(endHour) || startHour >= endHour) {
				return null;
			}

			return { startHour, endHour };
		}

		function hasTimeOverlap(firstRange, secondRange) {
			return firstRange.startHour < secondRange.endHour && secondRange.startHour < firstRange.endHour;
		}

		function findOverlapCourses(day, timeRange) {
			const newCourseRange = parseStartEndHours(timeRange);
			if (!newCourseRange) return [];

			const overlaps = [];

			for (const row of [...getSelectedRows(), ...getWaitlistedRows()]) {
				if (row.dataset.day !== day) continue;

				const existingRange = parseStartEndHours(row.dataset.time || "");
				if (!existingRange) continue;

				if (hasTimeOverlap(newCourseRange, existingRange)) {
					overlaps.push({
						code: row.dataset.code,
						time: row.dataset.time
					});
				}
			}

			return overlaps;
		}

		function renderPreviewTimetable() {
			previewTimetableBody.innerHTML = "";
			conflictList.innerHTML = "";

			const slotMap = {};
			const occupiedCells = new Set();
			const conflicts = [];

			getSelectedRows().forEach((row) => {
				const courseCode = row.dataset.code;
				const category = row.dataset.category || "N/A";
				const location = row.dataset.location || "TBA";
				const day = row.dataset.day;
				const time = row.dataset.time;
				const parsed = parseStartEndHours(time);

				if (!courseCode || !day || !previewDays.includes(day) || !parsed) return;

				for (let hour = parsed.startHour; hour < parsed.endHour; hour += 1) {
					if (hour < previewStartHour || hour >= previewEndHour) continue;
					const slotKey = `${day}|${hour}`;
					slotMap[slotKey] = slotMap[slotKey] || [];
					slotMap[slotKey].push({
						code: courseCode,
						category,
						location
					});
				}
			});

			for (let hour = previewStartHour; hour < previewEndHour; hour += 1) {
				const row = document.createElement("tr");

				const timeCell = document.createElement("td");
				timeCell.className = "preview-time-col";
				timeCell.textContent = `${formatHour(hour)} - ${formatHour(hour + 1)}`;
				row.appendChild(timeCell);

				previewDays.forEach((day) => {
					const occupiedKey = `${day}|${hour}`;
					if (occupiedCells.has(occupiedKey)) {
						return;
					}

					const cell = document.createElement("td");
					const slotKey = `${day}|${hour}`;
					const courses = slotMap[slotKey] || [];

					if (!courses.length) {
						cell.classList.add("preview-empty");
					} else {
						const hasConflict = courses.length > 1;

						if (hasConflict) {
							cell.classList.add("conflict-cell");
							conflicts.push(`${day} ${formatHour(hour)} - ${formatHour(hour + 1)}: ${courses.map((course) => course.code).join(", ")}`);

							courses.forEach((course) => {
								const block = document.createElement("div");
								block.className = "preview-course-block conflict-block";
								block.innerHTML = `
									<span class="preview-course-code">${course.code}</span>
									<span class="preview-course-meta">${course.category} | ${course.location}</span>
								`;
								cell.appendChild(block);
							});
						} else {
							const singleCourse = courses[0];
							const courseCode = singleCourse.code;
							let rowSpan = 1;

							for (let nextHour = hour + 1; nextHour < previewEndHour; nextHour += 1) {
								const nextCourses = slotMap[`${day}|${nextHour}`] || [];
								if (nextCourses.length !== 1 || nextCourses[0].code !== courseCode) {
									break;
								}
								rowSpan += 1;
							}

							if (rowSpan > 1) {
								cell.rowSpan = rowSpan;
								cell.classList.add("preview-merged");
								for (let coveredHour = hour + 1; coveredHour < hour + rowSpan; coveredHour += 1) {
									occupiedCells.add(`${day}|${coveredHour}`);
								}
							} else {
								cell.classList.add("preview-filled");
							}

							const block = document.createElement("div");
							block.className = "preview-course-block";
							block.innerHTML = `
								<span class="preview-course-code">${singleCourse.code}</span>
								<span class="preview-course-meta">${singleCourse.category} | ${singleCourse.location}</span>
							`;
							cell.appendChild(block);
						}
					}

					row.appendChild(cell);
				});

				previewTimetableBody.appendChild(row);
			}

			if (conflicts.length) {
				conflictPanel.style.display = "block";
				noConflictPanel.style.display = "none";

				conflicts.forEach((warning) => {
					const item = document.createElement("li");
					item.textContent = warning;
					conflictList.appendChild(item);
				});
			} else {
				conflictPanel.style.display = "none";
				noConflictPanel.style.display = "block";
			}
		}

		function addCourseRow(courseCode, credits, day, time, category, location) {
			const row = document.createElement("tr");
			row.dataset.code = courseCode;
			row.dataset.credits = String(credits);
			row.dataset.day = day;
			row.dataset.time = time;
			row.dataset.category = category;
			row.dataset.location = location;
			row.innerHTML = `
				<td>${courseCode}</td>
				<td>${credits}</td>
				<td>${day}</td>
				<td>${time}</td>
				<td>
					<div class="inline-actions">
						<button type="button" class="small-danger remove-course-btn" data-code="${courseCode}">Remove</button>
					</div>
				</td>
			`;

			selectedPlanBody.appendChild(row);
			toggleEmptyState();
			recalculateCredits();
			renderPreviewTimetable();
		}

		function addWaitlistRow(courseCode, day, time) {
			const row = document.createElement("tr");
			row.dataset.code = courseCode;
			row.dataset.day = day;
			row.dataset.time = time;
			row.innerHTML = `
				<td>${courseCode}</td>
				<td>${day}</td>
				<td>${time}</td>
				<td>
					<div class="inline-actions">
						<button type="button" class="small-danger remove-waitlist-btn" data-code="${courseCode}">Remove</button>
					</div>
				</td>
			`;

			waitlistBody.appendChild(row);
			toggleWaitlistEmptyState();
		}

		function removeCourse(courseCode) {
			const row = selectedPlanBody.querySelector(`tr[data-code="${courseCode}"]`);
			if (!row) return;

			row.remove();
			resetCourseButtonState(courseCode);
			toggleEmptyState();
			recalculateCredits();
			renderPreviewTimetable();
		}

		function removeWaitlistedCourse(courseCode) {
			const row = waitlistBody.querySelector(`tr[data-code="${courseCode}"]`);
			if (!row) return;

			row.remove();
			resetCourseButtonState(courseCode);
			toggleWaitlistEmptyState();
		}

		function handleOverlapDecision(courseCode, day, overlapCourses, actionLabel) {
			if (!overlapCourses.length) return true;

			const overlapMessage = overlapCourses
				.map((course) => `${course.code} (${course.time})`)
				.join(", ");

			const shouldReplace = window.confirm(
				`${courseCode} overlaps with ${overlapMessage} on ${day}.\n\nSelect OK to replace overlapping class(es) and ${actionLabel.toLowerCase()}, or Cancel to keep your current choices.`
			);

			if (!shouldReplace) return false;

			overlapCourses.forEach((course) => {
				if (selectedPlanBody.querySelector(`tr[data-code="${course.code}"]`)) {
					removeCourse(course.code);
					return;
				}

				removeWaitlistedCourse(course.code);
			});

			return true;
		}

		availableCoursesBody.addEventListener("click", (event) => {
			const button = event.target.closest("button[data-code]");
			if (!button) return;
			if (button.disabled) return;

			const row = button.closest("tr");
			if (!row) return;

			const courseCode = button.dataset.code;
			const creditCell = row.querySelector("td[data-credits]");
			const credits = Number(creditCell ? creditCell.dataset.credits : 0);
			const day = row.dataset.day;
			const time = row.dataset.time;
			const category = row.children[2] ? row.children[2].textContent.trim() : "N/A";
			const location = row.dataset.location || (row.children[6] ? row.children[6].textContent.trim() : "TBA");
			const availability = row.dataset.availability || "open";

			if (!courseCode || !credits || !day || !time) return;

			if (selectedPlanBody.querySelector(`tr[data-code="${courseCode}"]`)) return;
			if (waitlistBody.querySelector(`tr[data-code="${courseCode}"]`)) return;
			if (availability === "full") return;

			const overlapCourses = findOverlapCourses(day, time);
			if (availability === "waitlist") {
				if (!handleOverlapDecision(courseCode, day, overlapCourses, "add it to the waitlist")) return;
				addWaitlistRow(courseCode, day, time);
				setCourseButtonState(courseCode, "waitlisted");
				return;
			}

			if (!handleOverlapDecision(courseCode, day, overlapCourses, "add it to your plan")) return;

			addCourseRow(courseCode, credits, day, time, category, location);
			setCourseButtonState(courseCode, "added");
		});

		selectedPlanBody.addEventListener("click", (event) => {
			const button = event.target.closest(".remove-course-btn");
			if (!button) return;

			const courseCode = button.dataset.code;
			if (!courseCode) return;
			removeCourse(courseCode);
		});

		waitlistBody.addEventListener("click", (event) => {
			const button = event.target.closest(".remove-waitlist-btn");
			if (!button) return;

			const courseCode = button.dataset.code;
			if (!courseCode) return;
			removeWaitlistedCourse(courseCode);
		});

		clearPlanBtn.addEventListener("click", () => {
			getSelectedRows().forEach((row) => {
				resetCourseButtonState(row.dataset.code);
				row.remove();
			});

			getWaitlistedRows().forEach((row) => {
				resetCourseButtonState(row.dataset.code);
				row.remove();
			});

			toggleEmptyState();
			toggleWaitlistEmptyState();
			recalculateCredits();
			renderPreviewTimetable();
		});

		submitPlanBtn.addEventListener("click", () => {
			const totalCredits = Number(creditValue.textContent || 0);

			if (!getSelectedRows().length) {
				window.alert("Please add at least one course before submitting.");
				return;
			}

			if (totalCredits < 15 || totalCredits > 18) {
				const proceed = window.confirm("Your total credits are outside the recommended range. Submit anyway?");
				if (!proceed) return;
			}

			lastSavedPlanSignature = getCurrentPlanSignature();
			window.alert("Study plan submitted successfully.");
		});

		window.addEventListener("beforeunload", (event) => {
			if (!hasUnsavedPlanChanges()) return;

			event.preventDefault();
			event.returnValue = "";
		});

		if (availableCourseSearch) {
			availableCourseSearch.addEventListener("input", filterAvailableCourses);
		}

		Array.from(availableCoursesBody.querySelectorAll("tr")).forEach((row) => {
			const button = row.querySelector("button[data-code]");
			if (!button) return;

			setCourseButtonState(button.dataset.code, row.dataset.availability || "open");
		});

		getSelectedRows().forEach((row) => {
			if (row.dataset.code) {
				setCourseButtonState(row.dataset.code, "added");
			}
		});

		getWaitlistedRows().forEach((row) => {
			if (row.dataset.code) {
				setCourseButtonState(row.dataset.code, "waitlisted");
			}
		});

		undoPlanBtn.addEventListener("click", () => {
			if (!hasUnsavedPlanChanges()) return;

			const confirmed = window.confirm("Undo all changes and restore the original plan?");
			if (!confirmed) return;

			selectedPlanBody.innerHTML = initialSelectedHTML;
			waitlistBody.innerHTML = initialWaitlistHTML;

			Array.from(availableCoursesBody.querySelectorAll("tr")).forEach((row) => {
				const button = row.querySelector("button[data-code]");
				if (!button) return;
				setCourseButtonState(button.dataset.code, row.dataset.availability || "open");
			});

			getSelectedRows().forEach((row) => {
				if (row.dataset.code) setCourseButtonState(row.dataset.code, "added");
			});

			getWaitlistedRows().forEach((row) => {
				if (row.dataset.code) setCourseButtonState(row.dataset.code, "waitlisted");
			});

			toggleEmptyState();
			toggleWaitlistEmptyState();
			recalculateCredits();
			renderPreviewTimetable();
		});

		lastSavedPlanSignature = getCurrentPlanSignature();
		toggleEmptyState();
		toggleWaitlistEmptyState();
		recalculateCredits();
		renderPreviewTimetable();
