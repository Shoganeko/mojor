let captchaCode = ""

grecaptcha.ready(function() {
    grecaptcha.execute('6LckFPEUAAAAAHiDiRMPokGKUnfyEmdMiTE4Gq5S', { action: 'login' }).then(function(token) {
        captchaCode = token;
    });
});


(function () {
    const baseUrl = "http://localhost:8090"
    const apiBaseUrl = "http://localhost:8080"

    document.querySelector("#login").addEventListener("submit", function (e) {
        e.preventDefault();

        let status = document.querySelector("#status");

        $.ajax({
            url: `${apiBaseUrl}/v1/user`,
            type: 'POST',
            data: {
                username: $("#username").val(),
                password: sha512($("#password").val()),
                captcha: captchaCode
            },
            success: function (data) {
                status.textContent = "Signed in, updating session...";

                console.log(data);
                $.ajax({
                    url: `${baseUrl}/session`,
                    type: 'POST',
                    data: { token: data.payload.token.token },
                    success: function (e) {
                        window.location = `${baseUrl}/@self`;
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