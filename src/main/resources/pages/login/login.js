(function () {
    document.querySelector("#login").addEventListener("submit", function (e) {
        e.preventDefault();

        let status = document.querySelector("#status");

        $.ajax({
            url: 'https://api.shog.dev/v1/user',
            type: 'POST',
            data: {
                username: $("#username").val(),
                password: sha512($("#password").val()),
                captcha: grecaptcha.getResponse()
            },
            success: function (data) {
                status.textContent = "Signed in, updating session...";

                $.ajax({
                    url: 'https://shog.dev/session',
                    type: 'POST',
                    data: { token: data.token.token },
                    success: function (e) {
                        window.location = "https://shog.dev/account";
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