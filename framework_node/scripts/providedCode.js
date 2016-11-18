var uuid = require('node-uuid');
var id = uuid.v4()

var jsonDataPort = []
var host = window.location.hostname
var frames = 0

var dataManipulationFunction = (arg) => {}
var defineCommandToServer = () => {}

var wsPort = new WebSocket('ws://' + host + ':6000')
wsPort.onmessage = function (event) {
    jsonDataPort = JSON.parse(event.data)
}

var ws = new WebSocket('ws://' + host + jsonDataPort.port +"/?id="+id )
ws.onmessage = function (event) {
    frames++ 
    dataManipulationFunction(event.data)
}

var countFpsFunction = () =>
    {
      setTimeout(countFPS,1000)
      console.log("fps : " +frames)
      frames = 0
      console.log(JSON.stringify(jsonData,1))
    }
    setTimeout(countFPS,1000)

var sendCommand = () =>
{
    setTimeout(sendCommand,16.667)
    ws.send( defineCommandToServer() )
}
setTimeout(sendCommand,1000)

module.exports.dataManipulation = (f) => { dataManipulationFunction = f }
module.exports.commandToServer = (f) => { commandToServerFunction = f }
module.exports.countFps = () => { countFpsFunction() }