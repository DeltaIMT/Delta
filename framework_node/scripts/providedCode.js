

var dataManipulationFunction = (arg) => { }
var defineCommandToServer = () => { return "default message" }
var fps = 0
var ws
var getPing = () =>{}
module.exports.launch = () => {
    var uuid = require('node-uuid');
    var id = uuid.v4()

    var data = []
    var host = window.location.hostname
    var frames = 0

    var pingCallback
    var now1
    var ping


    var wsPort = new WebSocket('ws://' + host + ':9000' + "/?id=" + id)
    console.log("Searching port at 9000")

    wsPort.onmessage = function (event) {
        data = event.data
        console.log("Connection to : " + 'ws://' + host + ':' + data + "/?id=" + id)
        wsPort.close()        
        ws = new WebSocket('ws://' + host + ':' + data + "/?id=" + id)
        ws.onmessage = function (event) {
            var data = event.data

            if (data == "ping") {
                var now2 = new Date()
                ping = now2 - now1 // corresponds to the amount of milliseconds between now1 and now2
                pingCallback(ping)
                pingCallback = () => { }
            }
            else {
                frames++
                dataManipulationFunction(data)
            }
        }
    }

    getPing = (callback) => {
        ws.send("ping")
        now1 = new Date()
        pingCallback = callback
    }

    var countFpsFunction = () => {
        setTimeout(countFpsFunction, 1000)
        fps = frames
        frames = 0
    }
    setTimeout(countFpsFunction, 1000)

    var sendCommand = () => {
        setTimeout(sendCommand, 33.3)
        ws.send(defineCommandToServer())
    }

}
module.exports.dataManipulation = (f) => { dataManipulationFunction = f }
module.exports.startAutoSend = () => setTimeout(sendCommand, 1000)
module.exports.commandToServer = (f) => { defineCommandToServer = f }
module.exports.send = (str) => {
    ws.send(str)
}
module.exports.countFps = () => { return fps }
module.exports.getPing = (f) => getPing(f)