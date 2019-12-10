$( document ).ready(function () {
    let prevAuth = false;
    const owner = "324080061704794456";

    $("#result").text("Using: " + owner + " / Authorized: " + prevAuth);

    $("#submit").click(function () {
        const username = $("#username").val();
        const password = $("#password").val();
        const text = $("#text").val();

        if (!prevAuth) {
            $("#result").text("Authorizing...");

            // TODO encrypt client-side
            $.post("https://api.shog.dev/v1/user", { username: username, password: password, encr: true }, function (data) {
                const token = data.token.token;

                $.ajaxSetup({headers: {'Authorization': "token " + token}});
                prevAuth = true;

                updateMotd(text, owner);
            });

        } else updateMotd(text, owner);
    });
});

function updateMotd(text, owner) {
    $.post("https://api.shog.dev/motd", { text: text, owner: owner }, function () {
        $("#result").text("OK");
    });
}