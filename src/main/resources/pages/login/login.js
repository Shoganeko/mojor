var onloadCallback = function () {
    grecaptcha.render(document.getElementById("recaptcha"), {
        'theme': 'dark',
        'sitekey': '6Ldyv8sUAAAAALrxgLnAOZdWmqIneLT6HCEyh4E5'
    })
};

(function () {
    document.querySelector("#login").addEventListener("submit", function (e) {
        e.preventDefault();

        let status = document.querySelector("#status");

        $.ajax({
            url: 'http://localhost:8080/v1/user',
            type: 'POST',
            data: {
                username: $("#username").val(),
                password: sha512($("#password").val()),
                captcha: grecaptcha.getResponse()
            },
            success: function (data) {
                status.textContent = "Signed in, updating session...";

                $.ajax({
                    url: 'http://localhost:8080/session',
                    type: 'POST',
                    data: { token: data.token.token },
                    success: function (e) {
                        window.location = "http://localhost:8080/account";
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