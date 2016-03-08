function buildHtmlTable(selector, listData) {
    //reset
    $(selector).html("");

    // build
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

function onDoneDebug(response) {
    console.log(response);
}

function onError(response, statusText, errorThrown) {
    console.log(errorThrown);
}

// -------------------------------------------------------------------------

var hostUrl = "http://localhost:9001/";
var searchUrl = hostUrl + "search";
var testUrl = hostUrl + "test";
var hintTextSelector = "#testText";

var commonParam = {
    "async": true,
    "crossDomain": true,
    "headers": {
        "content-type": "application/json",
        "cache-control": "no-cache"
    },
    "processData": false
};

function listQueryResult(response) {
    console.log(JSON.stringify(response));
    buildHtmlTable("#excelDataTable", response);
}

function displayInText(response) {
    $(hintTextSelector).text("res: " + response.toString())
}

function getOptions() {
    var selectedFieldsEle = $("input[name='field']:checked");
    var fields = [];
    $.each(selectedFieldsEle, function () {
        fields.push($(this).attr("value"));
    });
    console.log(fields);
    return {
        "stem": $("#checkbox-stem").is(":checked"),
        "ignore": $("#checkbox-ignoreCase").is(":checked"),
        "swDict": $("#listbox-stopwords").val(),
        "fields": fields
    };
}

function parseQuery() {
    var searchContent = $("#searchBox").val().trim();
    if (searchContent.length == 0) {
        return null
    }
    return searchContent;
}

function indexIt() {
    var privateParam = {
        "method": "POST",
        "url": testUrl
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).done(onDoneDebug).error(onError);
}

function searchIt() {
    var options = getOptions();
    var searchContent = parseQuery();
    if (searchContent == null) {
        $(hintTextSelector).text("please input the search keywords");
        return;
    }
    var myParam = {
        "content": searchContent
    };
    $.extend(myParam, options);
    console.log("passed params: " + myParam);
    var privateParam = {
        "method": "GET",
        "data": $.param(myParam),
        "url": searchUrl
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).done(listQueryResult).error(onError);
}