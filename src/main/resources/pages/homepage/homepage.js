/**
 * Colors to rotate through.
 * @type {*[]}
 */
const colors = ["#e47d7d", "#f0b07f", "#dcdf8e", "#bddabb", "#81c6cc", "#92a7cc", "#a992be", "#c07387"];

(function () {
    // Get the last color used.
    let cycle = getCookie("cycle");

    // If the color doesn't exist, set to 0. If it does, get the number version.
    if (cycle === "")
        cycle = 0;
    else cycle = Number(cycle);

    // Make sure it doesn't reach the end.
    if (cycle++ >= colors.length - 1)
        cycle = 0;

    setCookie("cycle", cycle, 1);

    document.getElementById("motd").style.color = colors[cycle];
})();