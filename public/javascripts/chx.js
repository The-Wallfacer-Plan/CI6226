var myUrl = "http://localhost:9001/books";
var outputID = "#text-date";

var myData = JSON.stringify({
    "name": "TAOCP",
    "author": "Knuth"
});

function attrCollect() {
    var ret = {};
    var len = arguments.length;
    for (var i = 0; i < len; i++) {
        for (var p in arguments[i]) {
            if (arguments[i].hasOwnProperty(p)) {
                ret[p] = arguments[i][p];
            }
        }
    }
    return ret;
}

var commonParam = {
    "async": true,
    "crossDomain": true,
    "url": myUrl,
    "headers": {
        "content-type": "application/json",
        "cache-control": "no-cache"
    },
    "processData": false
};

function onDone(response) {
    console.log(response);
    $(outputID).text(JSON.stringify(response))
}

function onError(response, statusText, errorThrown) {
    console.log(errorThrown);
}

function testPOST() {
    console.log(myData);
    var privateParam = {
        "data": myData,
        "method": "POST"
    };
    param = attrCollect(commonParam, privateParam);
    $.ajax(param).done(onDone).error(onError);
}

function testGET() {
    console.log("start");
    var privateParam = {
        "method": "GET"
    };
    var param = attrCollect(commonParam, privateParam);
    $.ajax(param).done(onDone).error(onError);
}