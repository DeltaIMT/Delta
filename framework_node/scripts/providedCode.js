var uuid = require('node-uuid');
var id = uuid.v4()

var jsonData = []
var jsonDataPort = []
var host = window.location.hostname
var frames = 0

var wsPort = new WebSocket('ws://' + host + ':6000')
wsPort.onmessage = function (event) {
    jsonDataPort = JSON.parse(event.data)
}

var ws = new WebSocket('ws://' + host + jsonDataPort.port +"/?id="+id )
ws.onmessage = function (event) {
    frames++
    jsonData = JSON.parse(event.data) 
    jsonDataManipulation()
}
    
var sendCommandToServer = () => {
    var sendCommand = () =>
    {
    setTimeout(sendCommand,16.667)
    ws.send( JSON.stringify(defineCommandToServer())  )
    }
    setTimeout(sendCommand,1000)
}