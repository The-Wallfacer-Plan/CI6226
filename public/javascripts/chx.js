var myUrl = "http://localhost:9001/books";
var outputID = "#text-date";

var myData = JSON.stringify({
    "name": "TAOCP",
    "author": "Knuth"
});

var myList = [{"name": "abc", "age": 50},
    {"age": "25", "hobby": "swimming"},
    {"name": "xyz", "hobby": "programming"}];

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

// Adds a header row to the table and returns the set of columns.
// Need to do union of keys from all records as some records may not contain
// all records
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

function onGetDone(response) {
    console.log(JSON.stringify(response));
    buildHtmlTable("#excelDataTable", response);
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
    $.ajax(param).done(onGetDone).error(onError);
}