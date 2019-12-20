/**
 * Colors to rotate through.
 * @type {*[]}
 */
const colors = ["#e47d7d", "#f0b07f", "#dcdf8e", "#bddabb", "#81c6cc", "#92a7cc", "#a992be", "#c07387"];

(function () {
    // Get the last color used.
    let lastColor = getCookie("lastColor");

    // If there wasn't a last color used
    if (lastColor === "") {
        lastColor = 0;
        setCookie("lastColor", lastColor, 1)
    } else {
        // Get the index of the last color used from the cookie
        lastColor = Number(lastColor) + 1;

        if (lastColor >= colors.length)
            lastColor = 0;

        // Update to the new index
        setCookie("lastColor", lastColor, 1);
    }

    document.getElementById("motd").style.color = colors[lastColor];
})();