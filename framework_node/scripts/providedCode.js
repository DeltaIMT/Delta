

var dataManipulationFunction = (arg) => {}
var defineCommandToServer = () => {return "default message"}
var fps = 0
var ws
module.exports.launch = () => {
var uuid = require('node-uuid');
var id = uuid.v4()

var data = []
var host = window.location.hostname
var frames = 0



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
wsPort.send("ping") //enregistrer date1
wsPort.onmessage = function (event) {
    //enregistrer date2 et soustraire
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

}
module.exports.dataManipulation = (f) => { dataManipulationFunction = f }
module.exports.startAutoSend = () => setTimeout(sendCommand,1000)
module.exports.commandToServer = (f) => { defineCommandToServer = f }
module.exports.send = (str) =>  {    
    ws.send(str)} 
module.exports.countFps=  () => {return fps}