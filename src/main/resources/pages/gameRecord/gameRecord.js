(function () {
    refresh();
})();

function updateStatus(status) {

}

function refresh() {
    $.ajax({
        url: `http://localhost:8080/v1/user/games`,
        type: 'GET',
        beforeSend: function(xhr) {
            $.ajax({
                url: `http://localhost:8090/session`,
                async: false,
                success: function (data) {
                    xhr.setRequestHeader('Authorization', `token ${data}`);
                }
            });
        },
        success: function (data) {
            let payload = data.payload;
            let div = document.getElementById("games");

            if (payload.length === 0)
                div.innerHTML += "<p>No records!</p>"

            for (let i = 0; payload.length > i; i++) {
                let game = payload[i];

                div.innerHTML += getGameDiv(game.gameAsString, game.score, game.map, game.win, game.date);

                $(`#${date}-delete`).click(function () {
                    $.ajax({
                        url: `http://localhost:8080/v1/user/games`,
                        type: 'DELETE',
                        data: {
                            date: date
                        },
                        beforeSend: function(xhr) {
                            $.ajax({
                                url: `http://localhost:8090/session`,
                                async: false,
                                success: function (data) {
                                    xhr.setRequestHeader('Authorization', `token ${data}`);
                                }
                            });
                        },
                        success: function (data) {
                            updateStatus("Successfully deleted a record!")
                        },
                        error: function (e) {
                            updateStatus("There was an error that record!")
                        }
                    });
                });
            }
        },
        error: function (e) {
            updateStatus("There was an error refreshing!")
        }
    });
}

function getGameDiv(game, score, map, win, date) {
    let fDate = new Date(date);

    if (win === 1) {
        return `<div class='game'>` +
            `<div class='game-border'></div>` +
            `<h1 class='game-win'>W</h1>` +
            `<h3 class='game-text'>${game}: ${score} on ${map}</h3>` +
            `<span class="game-date">${fDate.toLocaleString("en-US")}</span>` +
            `<a href="#" id="${date}-delete"><span class="material-icons">delete</span></a>` +
            `</div>`
    } else {
        return `<div class='game'>` +
            `<div class='game-border'></div>` +
            `<h1 class='game-loss'>L</h1>` +
            `<h3 class='game-text'>${game}: ${score} on ${map}</h3>` +
            `<span class="game-date">${fDate.toLocaleString("en-US")}</span>` +
            `<a href="#" id="${date}-delete"><span class="material-icons">delete</span></a>` +
            `</div>`
    }
}