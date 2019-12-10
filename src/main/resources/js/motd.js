$( document ).ready(function () {
    let prevAuth = false;
    const owner = "324080061704794456";

    $("#result").text("Using: " + owner + " / Authorized: " + prevAuth);

    $("#submit").click(function () {
        const username = $("#username").val();
        const password = sha512($("#password").val());
        const text = $("#text").val();

        if (!prevAuth) {
            $("#result").text("Authorizing...");

            // TODO encrypt client-side
            $.post("https://api.shog.dev/v1/user", { username: username, password: password }, function (data) {
                const token = data.token.token;

                $.ajaxSetup({headers: {'Authorization': "token " + token}});
                prevAuth = true;

                updateMotd(text, owner);
            });

        } else updateMotd(text, owner);
    });
});

// Honestly dont know how this works, but it does :)
function sha512(str) {
    return crypto.subtle.digest("SHA-512", new TextEncoder("utf-8").encode(str)).then(buf => {
        return Array.prototype.map.call(new Uint8Array(buf), x=>(('00'+x.toString(16)).slice(-2))).join('');
    });
}

function updateMotd(text, owner) {
    $.post("https://api.shog.dev/motd", { text: text, owner: owner }, function () {
        $("#result").text("OK");
    });
}
