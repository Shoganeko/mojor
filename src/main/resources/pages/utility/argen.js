/**
 * The array's prefix.
 * @type {string}
 */
let currentArrayString = "arrayListOf(";

/**
 * The array's contents
 * @type {string}
 */
let currentContent = "";

/**
 * The ending of the array.
 * @type {string}
 */
let currentArrayStringEnding = ")";

(function () {
    // Clicking the kotlin button changes it to kotlin prefixes / suffixes
    document.querySelector("#kotlin").addEventListener("click", function (e) {
        e.preventDefault();
        currentArrayString = "arrayListOf(";
        currentArrayStringEnding = ")";
        update();
    });

    // Clicking the javascript button changes it to javascript prefixes / suffixes
    document.querySelector("#javascript").addEventListener("click", function (e) {
        e.preventDefault();
        currentArrayString = "[";
        currentArrayStringEnding = "]";
        update()
    });

    // When you press enter in the input, add it
    document.querySelector("#arEntry").addEventListener("keyup", function(event) {
        event.preventDefault();

        if (event.key.toLowerCase() === "enter") {submit()}
    });

    // Don't allow for submitting
    document.querySelector("#argen").addEventListener("submit", function (event) {
        event.preventDefault();
    });
})();

/**
 * Add the value from #arEntry to the contents.
 */
function submit() {
    let entry = document.querySelector("#arEntry").value;

    document.querySelector("#arEntry").value = "";

    if (entry !== "") {
        if (!isNaN(entry)) {
            currentContent += `${entry},`
        } else currentContent += `"${entry}",`
    }

    update();
}

/**
 * Update the result string.
 */
function update() {
    document.querySelector("#result").textContent = currentArrayString + currentContent.substr(0, currentContent.length - 1) + currentArrayStringEnding
}