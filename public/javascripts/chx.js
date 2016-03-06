var myUrl = "http://localhost:9001/books";
var representationOfDesiredState = "CHX";
var outputID = "#text-date";

function mytest() {
    $(outputID).text("good");

    var client = new XMLHttpRequest();
    client.open("POST", url, false);
    client.setRequestHeader("Content-Type", "application/json");
    client.send(representationOfDesiredState);

    $(outputID).text("lol");

    if (client.status == 200) {
        $(outputID).text("suc");
    } else {
        $(outputID).text("fail");
    }

}

function testalert() {
    $(outputID).text("lol");
    $.ajax({
        timeout: 4000,
        async: false,
        type: "POST",
        url: myUrl,
        data: {
            name: "hongxu",
            author: "chx"
        },
        success: function (rsp) {
            alert("succ " + rsp);
        },
        fail: function (rsp) {
            alert("fail " + rsp);
        },
        always: function (rsp) {
            alert("haha " + rsp);
        }
    });
    $(outputID).text("=||=");
}
