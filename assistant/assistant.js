const electron = require('electron');
let {
    ipcRenderer
} = require('electron');

let $ = require('jquery')
require('./jquery-ui-1.12.1/jquery-ui')

var toggleButton = $('#toggle-list-button')
var toggleButtonText = $('#toggle-list-button-text')
var assistantWindow = $('.assistant_window')
var assistantWindowMessage1 = $('#assistant_window_message_1')
var assistantWindowMessage2 = $('#assistant_window_message_2')




var currentErrorCount = -1;

toggleButton.on('click', function () {
    ipcRenderer.send('toggleList')
    // toggleButton.preventDefault()
})

ipcRenderer.on('errorCountUpdate', updateCount)
ipcRenderer.on('currentFileUpdate', updateFilename)

function updateCount(e, count) {
    if (count == currentErrorCount) {
        return;
    }
    else {
        currentErrorCount = count;
    }
    if (count == 1) {
        assistantWindowMessage1.text("Problem in")

    } else {
        assistantWindowMessage1.text("Probleme in")
    }

    if (count > 0) {
        animateBorderColor(assistantWindow, "rgb(239, 108, 0)")
        animateBackgroundColor(toggleButton, "rgb(239, 108, 0)")

    }else {
        animateBorderColor(assistantWindow, "teal")
        animateBackgroundColor(toggleButton, "teal")

    }



    toggleButtonText.fadeOut(100, function () {
        toggleButtonText.text(count)
        toggleButtonText.fadeIn(150)
    })


}

function animateBackgroundColor(element, color) {
    element.animate({
        backgroundColor: color
    }, 700)
}

function animateBorderColor(element, color) {
    element.animate({
        borderColor: color
    }, 700);
}


function updateFilename(e, filename) {
    assistantWindowMessage2.text(filename)
}