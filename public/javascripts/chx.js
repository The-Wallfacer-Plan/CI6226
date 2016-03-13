function onError(response, statusText, errorThrown) {
    var msg = "response " + response + "\tstatus " + statusText + "\terrorThrown " + errorThrown;
    console.log(msg);
}

var hostUrl = "http://localhost:9001/";
var hintTextSelector = "#statusPanel";
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

function displayResponse(msg) {
    var shown = JSON.stringify(msg, null, 2);
    $(hintTextSelector).show().text(shown).delay(1500).fadeOut();
}

function indexDoneHint(response) {
    displayResponse(response);
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

function getSearchContent() {
    var searchContent = $("#searchBox").val().trim();
    if (searchContent.length == 0) {
        return null
    }
    return searchContent;
}

function selectiveSearchIt(e) {
    console.log(e.code);
    if (e.code == "Enter") {
        searchIt()
    }
}

function searchIt() {

    var searchContent = getSearchContent();
    var param = $.param({
        "content": searchContent
    });
    //var param = encodeURIComponent(searchContent);
    console.log(param);
    if (searchContent == null) {
        displayResponse("please input the search keywords");
        return;
    }
    var privateParam = {
        "method": "GET",
        "data": param,
        "url": _getUrl("test")
    };
    $.extend(privateParam, commonParam);
    $.ajax(privateParam).error(onError);
    location.href = "?" + param
}