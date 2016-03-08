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

function onError(response, statusText, errorThrown) {
    console.log(errorThrown);
}

// -------------------------------------------------------------------------

var hostUrl = "http://localhost:9001/";
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

function _getUrl(uri) {
    return hostUrl + uri;
}

// --------------------------------------------------------------


function indexDoneHint(response) {
    // timer here
    $(hintTextSelector).text(response).show().delay(1000).fadeOut();
    console.log(response);
}

function getIndexOptions() {
    return {
        "stem": $("#cb-index-stem").is(":checked"),
        "ignore": $("#cb-index-ignore").is(":checked"),
        "swDict": $("#lb-index-stopwords").val()
    }
}

function indexIt() {
    var indexOptions = getIndexOptions();
    var privateParam = {
        "method": "POST",
        "url": _getUrl("indexDoc"),
        "data": JSON.stringify(indexOptions)
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).done(indexDoneHint).error(onError);
}


// --------------------------------------------------------------


function listSearchResult(response) {
    console.log(JSON.stringify(response));
    buildHtmlTable("#excelDataTable", response);
}


function getSearchOptions() {
    var selectedFieldsEle = $("input[name='field']:checked");
    var fields = [];
    $.each(selectedFieldsEle, function () {
        fields.push($(this).attr("value"));
    });
    console.log(fields);
    return {
        "fields": fields
    };
}

function getSearchContent() {
    var searchContent = $("#searchBox").val().trim();
    if (searchContent.length == 0) {
        return null
    }
    return searchContent;
}

function searchIt() {
    var options = getSearchOptions();
    var searchContent = getSearchContent();
    if (searchContent == null) {
        $(hintTextSelector).text("please input the search keywords");
        return;
    }
    var fullSearchData = {
        "content": searchContent
    };
    $.extend(fullSearchData, options);
    console.log("passed data: " + fullSearchData);
    var privateParam = {
        "method": "POST",
        "data": JSON.stringify(fullSearchData),
        "url": _getUrl("searchDoc")
    };
    $.extend(privateParam, commonParam);

    $.ajax(privateParam).done(listSearchResult).error(onError);
}

// ----------------------------------------------------------------------
var testUrl = _getUrl("test");