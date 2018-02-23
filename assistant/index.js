let $ = require('jquery')
require('./jquery-ui-1.12.1/jquery-ui')
window.$ = window.jQuery = require('./node_modules/jquery/dist/jquery.js');
window.Hammer = require('hammerjs');
Hammer = require('hammerjs');

require('materialize-css');
var WebSocket = require('ws')
const {
    clipboard
} = require('electron')


const {
    app,
    BrowserWindow,
    shell
} = require('electron')
const pauseable = require('pauseable');
const electron = require('electron');
let {
    ipcRenderer
} = require('electron');

//mainWindow.$ = $;
const fs = require("fs");


var remote = require('electron').remote;

var electronFs = remote.require('fs');

const path = require('path')

//const showListButton = document.getElementById('show-list-button');
const mainWindowDiv = $('#mainWindow');

let errorDescriptions = {};
readDescriptionFiles();
let openedFiles = {};
let activeFileErrorIds = {};
let activeFileErrorTimers = {};
var currentErrorCount = 0;
var userIdent = ""
let listWin;
loadDivsFromHtml()
var problemListDiv;
var problemListElementDiv;
var errDetailDiv;
var currentlyViewedError = 0;
var currentlyActiveFile = ""
var activeFileChanged
setupSocketConnection()

var socket

function readDescriptionFiles() {
    var dirPath = path.resolve('error_descriptions')



    var filepaths = electronFs.readdirSync(dirPath, 'utf8')

    for (let i = 0; i < filepaths.length; i++) {
        const filepath = filepaths[i];
        var absPath = path.resolve(dirPath, filepath)
        var data = electronFs.readFileSync(absPath, 'utf8');
        var dataJson = JSON.parse(data)

        var key = filepath.replace('.json', '').toLowerCase().replace(/[.,!'\"]/g, "");
        errorDescriptions[key] = dataJson;
    }




}

ipcRenderer.on('closeWindow', function () {
    if (currentlyViewedError != 0) {
        detailToListTrans(false);

    }
})

function loadDivsFromHtml() {


    mainWindowDiv.load(path.resolve('err_detail.html'), function () {
        errDetailDiv = $('#err-detail-container')
        errDetailDiv.find('.collapsible').collapsible();
    });

    mainWindowDiv.load(path.resolve('list.html'), function (data) {
        problemListDiv = $('#problem-list-container')


        problemListElementDiv = $('.list-item').hide()

        mainWindowDiv.on('click', '.list_row_main, .list_row_arrow', handleErrorClicked)

        mainWindowDiv.on('click', '.list_row_indicator', function (event) {
            sendCursorPosToServer(event.currentTarget.parentNode.id)
        })

        mainWindowDiv.on('click', '#problem-list-close', function () {
            ipcRenderer.send('toggleList')
            // toggleButton.preventDefault()
        })

        mainWindowDiv.on('click', '#problem-list-bottombar', function (event) {


            clipboard.writeText(userIdent)

            $('#problem-list-user-id').text("IN ZWISCHENABLAGE KOPIERT")
            setTimeout(() => {
                $('#problem-list-user-id').text("DEINE ID: " + userIdent)
            }, 3000);

        })

    });



}


function replaceDivs(target, oldDiv, newDiv) {
    oldDiv.fadeOut(100, function () {
        newDiv.hide();
        oldDiv.replaceWith(newDiv);
        newDiv.fadeIn(150)
    })

}


function addErrorListElement(errorId) {
    error = openedFiles[currentlyActiveFile]['errors'][errorId]
    var id = error.errorId

    var message = error.errorMessage
    var lineContent = error.lineContent
    var row = error.row
    var errortype = error.errorType.toLowerCase()
    var descriptionFile = errorDescriptions[errortype];


    var newListElement = problemListElementDiv.clone()

    newListElement.attr('id', id)

    newListElement.find('.list_row_indicator_title').text("Zeile:")
    newListElement.find('.list_row_indicator_number').text(row)
    newListElement.find('.list_row_main_title').text(descriptionFile.title)
    newListElement.find('.list_row_main_second_line').text(lineContent)
    // newListElement.find('.list_row_arrow').attr('id', id)
    newListElement.fadeIn(200)

    problemListDiv.find('.problem-list').append(newListElement);

    currentErrorCount += 1;

}

function openErrorDetail(errorJson) {

    replaceDivs(mainWindowDiv, problemListDiv, errDetailDiv)
    var errorId = errorJson.errorId
    var errortype = errorJson.errorType.toLowerCase()
    var highlightedText = errorJson.highlightedText
    var parentElement = errorJson.parentElement
    var descriptionFile = errorDescriptions[errortype];

    var description = replacePlaceholders(descriptionFile.description, errorJson)
    var shortExplanation = replacePlaceholders(descriptionFile.shortExplanation, errorJson)
    var longExplanation = replacePlaceholders(descriptionFile.longExplanation, errorJson)
    var solution = replacePlaceholders(descriptionFile.solution, errorJson)

    errDetailDiv.find('#err_detail_title').text(descriptionFile.title)

    errDetailDiv.find('#back-button').on('click', function () {
        currentlyViewedError = 0;
        detailToListTrans(false);
    })


    errDetailDiv.find('#jump-to-cursor').on('click', function (event) {
        sendCursorPosToServer(errorJson.errorId);
        ipcRenderer.send('blurAll');
        event.stopPropagation();

    })

    errDetailDiv.find('#err_detail_expl_description').html(description)
    errDetailDiv.find('#err_detail_expl_short_expl').html(shortExplanation)

    errDetailDiv.find('#err_detail_expl_long_expl').html(longExplanation)
    errDetailDiv.find('#err_detail_expl_solution').html(solution)
    errDetailDiv.find('.collapsible').collapsible();





    // var errorDetailDesc = $('<a>').attr('id', 'desc').attr('class', 'collection-item').append(JSON.stringify(description)).show()
    // errDetailDiv.find('#error_desc').html(errorDetailDesc)

}

function replacePlaceholders(text, errorJson) {
    return text.replace(/%SELECTED_TEXT%/g, '<span class="expl-code-element">' + errorJson.highlightedText + '</span>').replace(/%CONTAINING_METHOD%/g, '<span class="expl-code-element">' + errorJson.parentElement + '</span>')
        .replace(/%FILENAME%/g, '<span class="expl-code-element">' + errorJson.fileName + '</span>')
        .replace(/%ROW%/g, '<span class="expl-code-element">' + errorJson.row + '</span>')
        .replace(/%COLUMN%/g, '<span class="expl-code-element">' + errorJson.column + '</span>')


}

function handleErrorClicked(event) {

    var targetId = event.currentTarget.parentNode.id
    currentlyViewedError = targetId

    socket.send(JSON.stringify({
        "errorId": targetId,
        "requestType": "errorInfo"
    }))
}


function removeErrorListElement(id) {

    problemListDiv.find('#' + id).effect('blind', 700, function () {
        $(this).remove()
        updateErrorCount();

    })





}


function detailToListTrans(errorSolved) {
    $('.collapsible').collapsible('close', 0);
    $('.collapsible').collapsible('close', 1);
    if (errorSolved) {
        errDetailDiv.fadeTo(500, 0.5);
        var $toastContent = $('<span> <p id="line1">:)  Der Fehler wurde behoben</p> <p id="line2">Jetzt geht&#39s zurück zur Fehlerliste</p></span>').add($(''));

        Materialize.toast($toastContent, 4500, 'toasted', function () {
            errDetailDiv.fadeTo(500, 1.0, function () {
                replaceDivs(mainWindowDiv, errDetailDiv, problemListDiv)

            })

        })




    } else {
        replaceDivs(mainWindowDiv, errDetailDiv, problemListDiv)
    }
    currentlyViewedError = 0;
}

function getTimerForError(error) {
    var id = error.errorId
    var delay = error.timeout * 1000
    var timer = pauseable.setTimeout(() => {
        addErrorListElement(id)
        updateErrorCount();
        //timer.clear()
    }, delay);
    return timer;
}

function pauseTimers() {
    if (openedFiles[currentlyActiveFile] != null) {

        for (var errorId in openedFiles[currentlyActiveFile]['timers']) {
            openedFiles[currentlyActiveFile]['timers'][errorId].pause()
        }
    }
}

function updateTitleAndFooter(fileName, userId) {
    problemListDiv.find('#problem-list-title').text('Probleme in ' + fileName)
    userIdent = userId;
    problemListDiv.find('#problem-list-user-id').text("DEINE ID: " + userId)


}



function addErrors(fileName, errors) {


    if (openedFiles[fileName] == null) { //file war noch nie offen
        openedFiles[fileName] = {}
        openedFiles[fileName]['timers'] = {}
        openedFiles[fileName]['errors'] = {}
    }
    for (var error in errors) {
        var errorId = errors[error].errorId

        if (openedFiles[fileName]['timers'][errorId] == null) { // neuer error ohne timer
            openedFiles[fileName]['errors'][errorId] = errors[error];
            openedFiles[fileName]['timers'][errorId] = getTimerForError(errors[error])
        } else { // error und timer existiert bereits
            if (openedFiles[fileName]['timers'][errorId].isPaused()) {
                openedFiles[fileName]['timers'][errorId].resume()

            } else {
                addErrorListElement(errorId)

            }

        }
    }

}

function removeAllListElements() {
    problemListDiv.find('.problem-list').empty()


    // problemListDiv.find('.problem-list').children().fadeOut(100, function () {
    //     problemListDiv.find('.problem-list').empty();
    // });


    currentErrorCount = 0;
    updateErrorCount();
}

function sendCursorPosToServer(errorId) {
    socket.send(JSON.stringify({
        requestType: "setCursor",
        errorId: errorId
    }));

}



// function removeFromErrorIds(errors) {
//     for (var error in errors) {
//         var errorId = errors[error].errorId
//         delete activeFileErrorIds[errorId]
//         removeErrorListElement(errorId);
//         deleteTimerForError(errors[error])
//         if (currentlyViewedError == errorId) {
//             detailToListTrans(true);
//         }
//     }
// }

function removeErrors(fileName, errors) {
    for (error in errors) {
        errorId = errors[error].errorId
        var currentTimer = openedFiles[fileName]['timers'][errorId]
        if (currentTimer != null) {

            if (currentTimer.isDone()) {
                currentErrorCount -= 1;

            }

            currentTimer.clear()
            delete openedFiles[fileName]['timers'][errorId]
        }



        removeErrorListElement(errorId)
        if (currentlyViewedError == errorId) {
            detailToListTrans(true);
        }
    }

}

function updateErrors(fileName, errors) {
    for (error in errors) {
        var currentError = errors[error]
        errorId = currentError.errorId
        openedFiles[currentlyActiveFile]['errors'][errorId] = currentError

        var listElement = problemListDiv.find('#' + errorId)
        listElement.find('.list_row_indicator_number').text(currentError.row)
        listElement.find('.list_row_main_second_line').text(currentError.lineContent)


    }

}

function updateErrorCount() {



    if (currentErrorCount > 0) {
        problemListDiv.find('.titlebar').animate({
            backgroundColor: "rgb(239, 108, 0)"
        }, 700)

    } else {
        problemListDiv.find('.titlebar').animate({
            backgroundColor: "teal"
        }, 700)

    }
    if (problemListDiv.find('.list-item').length == 0) {
        problemListDiv.find('.problem-list').html('<p class="no-errors-message center-align"><span class="large-smiley">:)</span>   Keine Probleme erkannt</p>')
    } else {
        $('.no-errors-message').remove()
    }


    ipcRenderer.send('errorCountUpdate', currentErrorCount)
}

function setupSocketConnection() {

    socket = new WebSocket('ws://127.0.0.1:80');



    socket.onopen = function (event) {
        socket.send(JSON.stringify({
            requestType: "update"
        }))
    }

    socket.onmessage = function (event) {
        var receivedJson = JSON.parse(event.data)

        if (receivedJson.activeFileChanged != null) {
            var fileNameExt = JSON.stringify(receivedJson.fileName)
            var fileName = fileNameExt.replace('.java', '')
            activeFileChanged = receivedJson.activeFileChanged
            userId = receivedJson.userName

            if (activeFileChanged) {
                removeAllListElements();

                updateTitleAndFooter(fileNameExt, userId);
                ipcRenderer.send('currentFileUpdate', fileNameExt)
                pauseTimers();

            }
            currentlyActiveFile = fileName
            addErrors(fileName, receivedJson.added.errors)
            removeErrors(fileName, receivedJson.removed.errors)
            updateErrors(fileName, receivedJson.updated.errors)
            updateErrorCount()
            // addToErrorIds(receivedJson.added.errors)
            // removeFromErrorIds(receivedJson.removed.errors)


        } else if (receivedJson.errorId != null) {
            openErrorDetail(receivedJson)

        }



    }

    socket.onerror = function (event) {
        console.log("socket on error");


    }
    socket.onclose = function (event) {
        console.log("socket on close")
        setTimeout(() => {
            setupSocketConnection()
        }, 5000);
    }
}