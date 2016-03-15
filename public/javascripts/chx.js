function onError(response, statusText, errorThrown) {
    var msg = "response " + response + "\tstatus " + statusText + "\terrorThrown " + errorThrown;
    console.log(msg);
}

var hostUrl = "http://localhost:9001/";
var hintTextSelector = "#infoBox";
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
    var paramObj = {
        "content": searchContent
    };
    $.extend(paramObj, getLOptions());
    var param = $.param(paramObj);
    if (searchContent == null) {
        return;
    }
    location.href = "?" + param
}