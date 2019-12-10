/**
 * If authorization has previously taken place.
 * @type {boolean}
 */
let isAuthorized = false;

/**
 * Current owner.
 * @type {number}
 */
let currentOwner = -1;

/**
 * The result
 * @type {Element}
 */
const result = document.querySelector("#result");

(function () {
    result.textContent = `Using: ${currentOwner} / Authorized: ${isAuthorized}\n\nReload to un-authorize!`;

    document.querySelector("#submit").addEventListener('click', function () {
        result.textContent = "Finding data...";

        const password = sha512(document.querySelector("#password").value);
        const username = document.querySelector("#username").value;
        const text = document.querySelector("#text").value;

        if (!isAuthorized) {
            result.textContent = "Attempting to authorize...";

            $.post("https://api.shog.dev/v1/user", { username: username, password: password }, function (data) {
                const token = data.token.token;

                $.ajaxSetup({headers: {'Authorization': "token " + token}});

                isAuthorized = true;
                currentOwner = data.user.id;

                updateMotd(text, currentOwner);
            });

        } else updateMotd(text, currentOwner);
    });
})();

/**
 * Update the MOTD by sending a REST request.
 * @param text The MOTD contents.
 * @param owner The owner of the MOTD (ID)
 */
function updateMotd(text, owner) {
    $.post("https://api.shog.dev/motd", { text: text, owner: owner }, function () {
        document.querySelector("#result").textContent = "OK";
    });
}
