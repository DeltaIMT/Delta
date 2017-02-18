function generateUUID(){
    var d = new Date().getTime();
    if(window.performance && typeof window.performance.now === "function"){
        d += performance.now(); //use high-precision timer if available
    }
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
}

var dataManipulationFunction = (arg) => { }
var defineCommandToServer = () => { return "default message" }
var fps = 0
var ws
var getPing = () => { }
module.exports.launch = () => {
    var id = generateUUID()

    var data = []
    var host = window.location.hostname
    var frames = 0

    var pingCallback
    var now1
    var ping


    const wsPort = new WebSocket('ws://' + host + ':9000' + "/?id=" + id)
    console.log("Searching port at 9000")

    wsPort.onmessage =  (event) => {
        data = event.data
        console.log("Connection to : " + 'ws://' + host + ':' + data + "/?id=" + id)

        ws = new WebSocket('ws://' + host + ':' + data + "/?id=" + id)
        ws.onmessage =  (event)=>  {
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
        wsPort.close(1000, "job done")
    }


    wsPort.onerror = (err) => {
        console.log("wsPort Error : " + err)
    }

    wsPort.onclose = (e) => {
        console.log("wsPort Close : " + e)
    }

    wsPort.onopen = (e) => {
        console.log("wsPort Open : " + e)
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

}
module.exports.dataManipulation = (f) => { dataManipulationFunction = f }
module.exports.send = (str) => {
    ws.send(str)
}
module.exports.countFps = () => { return fps }
module.exports.getPing = (f) => getPing(f)