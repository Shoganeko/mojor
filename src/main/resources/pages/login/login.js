(function () {
    document.querySelector("#login").addEventListener("submit", function (e) {
        e.preventDefault();

        let username = document.querySelector("#username");
        let password = document.querySelector("#password");
        let status = document.querySelector("#status");

        $.ajax({
            url: 'https://api.shog.dev/v1/user',
            type: 'POST',
            data: {
                username: $("#username").val(),
                password: sha512($("#password").val())
            },
            success: function (data) {
                status.textContent = "Signed in, updating session...";

                $.ajax({
                    url: 'http://localhost:8090/session',
                    type: 'POST',
                    data: { token: data.token.token },
                    success: function (e) {
                        window.location = "http://localhost:8090/account";
                    },
                    error: function (e) {
                        status.textContent = "Failed to update session! Please try again."
                    }
                })
            },
            error: function (e) {
                status.textContent = "Invalid credentials."
            }
        });
    });
})();