$( document ).ready(function () {
    $("#strlen").submit(function (e) {
        e.preventDefault();

        $("#result").text($("#str").val().length + " characters long.")
    });
});