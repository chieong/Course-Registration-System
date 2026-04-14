(function () {
  function bindControls() {
    var printBtn = document.getElementById("printBtn");
    var closeBtn = document.getElementById("closeBtn");

    if (printBtn) {
      printBtn.addEventListener("click", function () {
        window.print();
      });
    }

    if (closeBtn) {
      closeBtn.addEventListener("click", function () {
        window.close();
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bindControls);
    return;
  }

  bindControls();
})();
