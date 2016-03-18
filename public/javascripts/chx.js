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

function indexIt() {
    var indexOptions = getLOptions();
    var privateParam = {
        "method": "POST",
        "url": _getUrl("indexDoc"),
        "data": JSON.stringify(indexOptions)
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).done(indexDoneHint).error(onError);
}

// --------------------------------------------------------------

function getSearchContent() {
    var searchContent = $("#searchBox").val().trim();
    if (searchContent.length == 0) {
        return null
    }
    return searchContent;
}

function selectiveSearchIt(e) {
    if (e.code == "Enter") {
        searchIt()
    }
}

function searchIt() {
    var searchContent = getSearchContent();
    var topN = $("#topNSelect").val();
    var paramObj = {
        "content": searchContent,
        "topN": topN
    };
    $.extend(paramObj, getLOptions());
    var param = $.param(paramObj);
    if (searchContent == null) {
        return;
    }
    location.href = "?" + param
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