

var dataManipulationFunction = (arg) => {}
var defineCommandToServer = () => {return "default message"}

module.exports.launch = () => {
var uuid = require('node-uuid');
var id = uuid.v4()

var data = []
var host = window.location.hostname
var frames = 0
var fps = 0

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
    setTimeout(sendCommand,16.667)
    ws.send( commandToServerFunction() )
}
setTimeout(sendCommand,1000)
}

var target = 'TODO' //TODO

// using net-ping TODO trouble to install package
var pingTest1 = () =>
{
    var ping = require ("net-ping")

    session.pingHost (target, function (error, target, sent, received) {
        var ms = received - sent
        if (error)
            console.log (target + ": " + error.toString ())
        else
            console.log (target + ": Alive (ms=" + ms + ")")
    })
}
// using ping-wrapper
var pingTest2 = () =>
{
    var Ping = require('ping-wrapper')
    Ping.configure()

    var ping = new Ping(target)

    ping.on('ping', function(data){
        console.log('Ping %s: time: %d ms', data.host, data.time)
    })

    ping.on('fail', function(data){
        console.log('Fail', data)
    })

    //ping.stop()
}
// using tcp-ping
var pingTest3 = () =>
{
    var port = TODO //TODO
    var tcpp = require('tcp-ping')

    tcpp.probe(target, port, function(err, available) {
        console.log(available)
    })

    tcpp.ping({ address: target }, function(err, data) {
        console.log(data)
    })
}
// using jjg-ping
var pingTest4 = () =>
{
    var ping = require('jjg-ping')

    ping.system.ping(target, function(latency, status) {
        if (status) {
            console.log('The target is reachable (' + latency + ' ms ping).')
        }
        else {
            console.log('The target is unreachable.')
        }
    })
}
//TODO use ping ? hard to code

module.exports.dataManipulation = (f) => { dataManipulationFunction = f }
module.exports.commandToServer = (f) => { defineCommandToServer = f }
module.exports.countFps = () => { return fps }
module.exports.pingMeasurement = () => { return pingTest2() }