const {
  app,
  BrowserWindow,
  shell
} = require('electron')

const path = require('path')
const url = require('url')
var WebSocket = require('ws')


// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let listWin
let assistantWin
const electron = require('electron');


let {
  ipcMain
} = require('electron');

ipcMain.on('toggleList', function (e) {
  if (listWin.isVisible()) {
    listWin.send('closeWindow')
    listWin.hide()
  } else {
    listWin.show()

  }
  // listWin.toggleDevTools()
})

ipcMain.on('blurAll', function (e) {
  listWin.blur();
  assistantWin.blur();
})

ipcMain.on('log', function (e, message) {
  // console.log('logged from js:', message);
})

ipcMain.on('errorCountUpdate', function (e, count) {
  assistantWin.send('errorCountUpdate', count);
})

ipcMain.on('currentFileUpdate', function (e, filename) {
  assistantWin.send('currentFileUpdate', filename)
})



function createWindows() {

  var appScreen = electron.screen;
  var screenDimensions = appScreen.getPrimaryDisplay().size
  console.log('dimensions: ', screenDimensions);



  assistantWin = new BrowserWindow({
    width: 300,
    height: 75,
    x: screenDimensions.width - 320,
    y: screenDimensions.height - 150,
    frame: false,
    transparent: true,
    show: true,
    skipTaskbar: false,
    movable: true,
    resizable: false

  })
  //win.setResizable(false)
  assistantWin.setAlwaysOnTop(true)

  assistantWin.once('ready-to-show', () => {
    assistantWin.show()
    // assistantWin.toggleDevTools()

  })

  // and load the index.html of the app.
  assistantWin.loadURL(url.format({
    pathname: path.join(__dirname, 'assistant.html'),
    protocol: 'file:',
    slashes: true
  }))

  assistantWin.on('close', () => {
    listWin.destroy();

  })

  // Emitted when the window is closed.
  assistantWin.on('closed', () => {
    assistantWin = null
  })

  var listWidth = 300
  var listHeight = 600
  // Create the browser window.
  listWin = new BrowserWindow({
    width: listWidth,
    height: listHeight,
    x: screenDimensions.width - listWidth - 20,
    y: screenDimensions.height - listHeight - 200,
    frame: false,
    transparent: false,
    show: false,
    parent: assistantWin,
    skipTaskbar: true,
    movable: true,
    resizable: true



  })
  //win.setResizable(false)
  listWin.setAlwaysOnTop(true)


  listWin.once('ready-to-show', () => {
    listWin.hide()



  })

  // and load the index.html of the app.
  listWin.loadURL(url.format({
    pathname: path.join(__dirname, 'index.html'),
    protocol: 'file:',
    slashes: true
  }))




  // Open the DevTools.
  //win.webContents.openDevTools()

  // Emitted when the window is closed.
  listWin.on('close', (e) => {
    e.preventDefault();
    listWin.hide()

  })
  listWin.on('closed', () => {
    // Dereference the window object, usually you would store windows
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    // listWin = null
  })







}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', function () {
  createWindows()
})

// Quit when all windows are closed.
app.on('window-all-closed', () => {
  // On macOS it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  // On macOS it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (listWin === null) {
    createWindow()
  }
})