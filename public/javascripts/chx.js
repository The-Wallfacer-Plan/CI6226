var hostUrl = "http://localhost:9001/";
var queryUrl = hostUrl + "search";
var testUrl = hostUrl + "test";

var myData = JSON.stringify({
    "name": "TAOCP",
    "author": "Knuth"
});

function buildHtmlTable(selector, listData) {
    //reset
    $(selector).html("");

    var columns = addAllColumnHeaders(listData, selector);
    for (var i = 0; i < listData.length; i++) {
        var row$ = $('<tr/>');
        for (var colIndex = 0; colIndex < columns.length; colIndex++) {
            var cellValue = listData[i][columns[colIndex]];

            if (cellValue == null) {
                cellValue = "";
            }

            row$.append($('<td/>').html(cellValue));
        }
        $(selector).append(row$);
    }
}

function addAllColumnHeaders(listData, selector) {
    var columnSet = [];
    var headerTr$ = $('<tr/>');
    for (var i = 0; i < listData.length; i++) {
        var rowHash = listData[i];
        for (var key in rowHash) {
            if ($.inArray(key, columnSet) == -1) {
                columnSet.push(key);
                headerTr$.append($('<th/>').html(key));
            }
        }
    }
    $(selector).append(headerTr$);
    return columnSet;
}

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
    "headers": {
        "content-type": "application/json",
        "cache-control": "no-cache"
    },
    "processData": false
};

function onDone(response) {
    console.log(response);
}

function onError(response, statusText, errorThrown) {
    console.log(errorThrown);
}

function listQueryResult(response) {
    console.log(JSON.stringify(response));
    buildHtmlTable("#excelDataTable", response);
}

function searchIt() {
    console.log("start");
    var privateParam = {
        "method": "GET",
        "url": testUrl
    };
    var param = attrCollect(commonParam, privateParam);
    $.ajax(param).done(listQueryResult).error(onError);
}

//-----------------------------------------------------------

function displayInText(response) {
    $("#testText").text("res: " + response.toString())
}

function testPOST() {
    console.log(myData);
    var privateParam = {
        "data": myData,
        "method": "POST",
        "url": testUrl
    };
    param = attrCollect(commonParam, privateParam);
    $.ajax(param).done(onDone).error(onError);
}

function testGet() {
    var myParam = {
        "foo": "3=g",
        "bar": "32 gj"
    };
    console.log("start");
    var privateParam = {
        "method": "GET",
        "data": $.param(myParam),
        "url": testUrl
    };
    var param = attrCollect(commonParam, privateParam);
    $.ajax(param).done(displayInText).error(onError);
}