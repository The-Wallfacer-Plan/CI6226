function onError(response, statusText, errorThrown) {
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

// --------------------------------------------------------------

function displayResponse(msg) {
    var shown = JSON.stringify(msg, null, 2);
    $(hintTextSelector).text(shown);
}

function indexDoneHint(response) {
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
    $.ajax(privateParam).done(indexDoneHint).error(onError);
}

// --------------------------------------------------------------

function getSearchContent() {
    return $("#searchBox").val().trim();
}

function selectiveSearchIt(e) {
    if (e.code == "Enter") {
        bSearch()
    }
}

function bSearch() {
    var searchContent = getSearchContent();
    var topN = $("#topNSelect").val();
    var paramObj = {
        "content": searchContent,
        "topN": topN
    };
    $.extend(paramObj, getLOptions());
    var param = $.param(paramObj);
    location.href = "?" + param;
}

//------------------------------------------------
function readTheDocs() {
    var form = document.createElement("form");
    form.method = "GET";
    form.action = "http://lucene.apache.org/core/5_5_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview";
    form.target = "_blank";
    document.body.appendChild(form);
    form.submit();
}

// ------------------------------------------------

var app1Url = hostUrl + "app1";

function a1QueryInfo() {
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
    var lOption = getLOptions();
    var searchInfo = a1QueryInfo();
    var topN = $("#topNSelect").val();
    var paramObj = {
        "topN": topN
    };
    $.extend(paramObj, lOption, searchInfo);
    var param = $.param(paramObj);
    location.href = app1Url + "?" + param
}

// ------------------------------------------------

function a2Search() {
    var searchContent = getSearchContent();
    var topN = $("#topNSelect").val();
    var paramObj = {
        "content": searchContent,
        "topN": topN
    };
    $.extend(paramObj, getLOptions());
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
    $.ajax(privateParam).done(indexDoneHint).error(onError);
}