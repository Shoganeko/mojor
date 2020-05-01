/**
 * If this is checked, then it is a win.
 */
let win = document.querySelector("#win");

let overwatch = document.querySelector("#overwatch");
let csgo = document.querySelector("#overwatch");
let valorant = document.querySelector("#valorant");

let score = document.querySelector("#score");
let map = document.querySelector("#map");
let status = document.querySelector("#status");

(function () {
    let game = 1

    if (overwatch.checked)
        game = 2
    else if (valorant.checked)
        game = 3

    $("#submit").click(function () {
        $.ajax({
            url: `https://api.shog.dev/v1/user/games`,
            type: 'POST',
            beforeSend: function(xhr) {
                $.ajax({
                    url: `https://shog.dev/session`,
                    async: false,
                    success: function (data) {
                        xhr.setRequestHeader('Authorization', `token ${data}`);
                    }
                });
            },
            data: {
                win: win.checked,
                score: score.value,
                map: map.value,
                game: game
            },
            success: function (data) {
                status.textContent = "Successfully added game report."
            },
            error: function (e) {
                status.textContent = "Could not add game report!"
            }
        });
    });
})();