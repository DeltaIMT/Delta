

var dataManipulationFunction = (arg) => {}
var defineCommandToServer = () => {return "default message"}
var fps = 0
module.exports.launch = () => {
var uuid = require('node-uuid');
var id = uuid.v4()

var data = []
var host = window.location.hostname
var frames = 0


var ws
var wsPort = new WebSocket('ws://' + host + ':9000'+"/?id="+id)
console.log("Searching port at 9000")
wsPort.onmessage = function (event) {
    data = event.data
console.log("Connection to : "+'ws://' + host +':'+data +"/?id="+id)
    ws = new WebSocket('ws://' + host +':'+data +"/?id="+id )
    ws.onmessage = function (event) {
        frames++ 
        dataManipulationFunction(event.data)
    }
}

var countFpsFunction = () =>
{
    setTimeout(countFpsFunction,1000)
    fps = frames
    frames = 0
}
setTimeout(countFpsFunction,1000)

var sendCommand = () =>
{
    setTimeout(sendCommand,33.3)
    ws.send( defineCommandToServer() )
}
setTimeout(sendCommand,1000)
}
module.exports.dataManipulation = (f) => { dataManipulationFunction = f }
module.exports.commandToServer = (f) => { defineCommandToServer = f }
module.exports.countFps=  () => {return fps}