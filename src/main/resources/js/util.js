/**
 * Capitalize the first letter of the word.
 * @param word The word to capitalize
 * @returns {string}
 */
function capitalize(word) {
    if (typeof word !== 'string') return '';

    return word.charAt(0).toUpperCase() + word.slice(1);
}

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
