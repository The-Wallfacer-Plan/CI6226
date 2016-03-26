function _onError(response, statusText, errorThrown) {
    var msg = "response " + JSON.stringify(response) + "\tstatus " + statusText + "\terrorThrown " + errorThrown;
    console.log(msg);
}

var hostUrl = location.origin + "/";
var hintTextSelector = "#infoBox";
var commonParam = {
    "async": true,
    //"crossDomain": true,
    "headers": {
        "content-type": "application/json",
        "cache-control": "no-cache"
    },
    "processData": false
};

$.support.cors = true;

function _getUrl(uri) {
    return hostUrl + uri;
}

function getLOptions() {
    return {
        "stem": $("#cb-index-stem").is(":checked"),
        "ignore": $("#cb-index-ignore").is(":checked"),
        "swDict": $("#lb-index-stopwords").val()
    }
}

function getSOptions() {
    return {
        "topN": $("#topNSelect").val(),
        "similarity": $("#lb-s-similarity").val()
    }
}

// --------------------------------------------------------------

function displayResponse(msg) {
    var shown = JSON.stringify(msg, null, 2);
    $(hintTextSelector).text(shown);
}

function _indexDoneHint(response) {
    console.log(JSON.stringify(response));
    displayResponse(response);
}

function bIndex() {
    var indexOptions = getLOptions();
    indexOptions["reIndex"] = $("#cb-force-index").is(":checked");
    var privateParam = {
        "method": "POST",
        "url": hostUrl,
        "data": JSON.stringify(indexOptions)
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).done(_indexDoneHint).error(_onError);
}

// --------------------------------------------------------------

function _getSearchContent() {
    return $("#searchBox").val().trim();
}

function selectiveSearchIt(e) {
    if (e.code == "Enter") {
        bSearch()
    }
}

function bSearch() {
    var searchContent = _getSearchContent();
    var paramObj = {
        "content": searchContent
    };
    $.extend(paramObj, getSOptions(), getLOptions());
    var param = $.param(paramObj);
    location.href = "?" + param;
}

//------------------------------------------------

function _openUrlInNewTab(url) {
    console.log(url);
    var form = document.createElement("form");
    form.method = "GET";
    form.action = url;
    form.target = "_blank";
    document.body.appendChild(form);
    form.submit();
}

function readTheDocs() {
    var url = "http://lucene.apache.org/core/5_5_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview";
    _openUrlInNewTab(url);
}

function references() {
    var url = "/assets/resources/outfile.txt";
    _openUrlInNewTab(url);
}

// ------------------------------------------------

function _a1QueryParam() {
    var pubYear = $("#app1-pubYear").val();
    var venue = $("#app1-venue").val().trim();
    var authors = $("#app1-authors").val().trim();
    var info = {
        "pubYear": pubYear
    };
    if (venue.length != 0) {
        info["venue"] = venue
    }
    if (authors.length != 0) {
        info["authors"] = authors
    }
    return info
}

function a1Search() {
    var paramObj = _a1QueryParam();
    $.extend(paramObj, getSOptions(), getLOptions());
    var param = $.param(paramObj);
    location.href = "?" + param
}

// ------------------------------------------------

function _a2QueryParam() {
    var pubYear = $("#app2-pubYear").val().trim();
    var venue = $("#app2-venue").val().trim();
    var info = {
        "pubYear": pubYear
    };
    if (venue.length != 0) {
        info["venue"] = venue;
    }
    return info
}

function a2Search() {
    var paramObj = _a2QueryParam();
    $.extend(paramObj, getSOptions(), getLOptions());
    var param = $.param(paramObj);
    location.href = "?" + param;
}

function a2Index() {
    var indexOptions = getLOptions();
    indexOptions["reIndex"] = $("#cb-force-index").is(":checked");
    var privateParam = {
        "method": "POST",
        "url": _getUrl("app2"),
        "data": JSON.stringify(indexOptions)
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).done(_indexDoneHint).error(_onError);
}