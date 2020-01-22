/**
 * The current language rotate position.
 * @type {number}
 */
let currentLangRotatePos = 0;

/**
 * The available languages.
 * @type {[string, string, string]}
 */
let availableLanguages = ["kotlin", "java", "javascript", "python"];

/**
 * The array's prefix.
 * @type {string}
 */
let currentArrayString = "val array = arrayListOf(";

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
    let retCookie = getCookie("arGenLangRotatePos");

    if (retCookie !== "") {
        currentLangRotatePos = Number(retCookie);
        setLang(availableLanguages[currentLangRotatePos]);
    }

    currentContent = getCookie("arGenContents");
    update();

    // Clicking the lang-swap button changes the language
    document.querySelector("#lang-swap").addEventListener("click", function (e) {
        e.preventDefault();

        let newPos = currentLangRotatePos + 1;

        if (newPos >= availableLanguages.length)
            newPos = 0;

        currentLangRotatePos = newPos;

        setCookie("arGenLangRotatePos", currentLangRotatePos, 6);

        setLang(availableLanguages[currentLangRotatePos]);

        update();
    });

    // Clear text
    document.querySelector("#clear").addEventListener("click", function (e) {
        e.preventDefault();

        currentContent = "";
        setCookie("arGenContents", currentContent, 6);

        update();
    });

    // When you press enter in the input, add it
    document.querySelector("#arEntry").addEventListener("keyup", function(event) {
        event.preventDefault();

        if (event.key.toLowerCase() === "enter")
            submit()
    });

    // Don't allow for submitting
    document.querySelector("#argen").addEventListener("submit", function (event) {
        event.preventDefault();
    });
})();

/**
 * Set the language.
 * @param lang The new language
 */
function setLang(lang) {
    switch (lang) {
        case "kotlin":
            currentArrayString = "val array = arrayListOf(";
            currentArrayStringEnding = ")";
            break;
        case "javascript":
            currentArrayString = "const array = [";
            currentArrayStringEnding = "];";
            break;
        case "java":
            currentArrayString = "String[] array = {";
            currentArrayStringEnding = "};";
            break;
        case "python":
            currentArrayString = "array = [";
            currentArrayStringEnding = "]";
            break;
    }

    document.querySelector("#lang-swap").textContent = `${capitalize(lang)}: Click to Swap`
}

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

    setCookie("arGenContents", currentContent, 6);
    update();
}

/**
 * Update the result string.
 */
function update() {
    document.querySelector("#result").textContent = currentArrayString + currentContent.substr(0, currentContent.length - 1) + currentArrayStringEnding
}