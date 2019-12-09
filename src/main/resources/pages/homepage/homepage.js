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
        lastColor = -1;
        setCookie("lastColor", lastColor, 1)
    } else {
        // Get the index of the last color used from the cookie
        lastColor = Number(lastColor);
        lastColor++;

        if (lastColor >= 7)
            lastColor = 0;

        // Update to the new index
        setCookie("lastColor", lastColor, 1);
    }

    document.getElementById("motd").style.color = colors[lastColor];
})();

/**
 * Set cookie
 * @param cookieName The cookie's name
 * @param cookieValue The new cookie value
 * @param expireDays When to expire (in days)
 */
function setCookie(cookieName, cookieValue, expireDays) {
    let date = new Date();

    date.setTime(date.getTime() + (expireDays * 24 * 60 * 60 * 1000));

    document.cookie = `${cookieName}=${cookieValue};expires=${date.toUTCString()};path=/`;
}

/**
 * Get a cookie by their name.
 * @param cookieName The cookie's name
 * @returns {string}
 */
function getCookie(cookieName) {
    let name = `${cookieName}=`;
    let cookies = document.cookie.split(';');

    for(let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i];

        while (cookie.charAt(0) === ' ') { cookie = cookie.substring(1); }

        if (cookie.indexOf(name) === 0) {
            return cookie.substring(name.length, cookie.length);
        }
    }

    return "";
}