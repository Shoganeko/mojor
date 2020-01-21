(function () {
    document.querySelector("#strlen").addEventListener("submit", function (event) {
        event.preventDefault();

        document.querySelector("#result").textContent =
            document.querySelector("#str").value.length + " characters long."
    });
})();