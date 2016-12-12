var WebSocket = require('websocket').client;
var host = '127.0.0.1'
var uuid = require('node-uuid');
var id_g = uuid.v4()
var k = 0
var N = 50

var createOne = () => {

    var k2 = k
    k++
    if (k < N) {

        setTimeout(createOne, 150)
    }


    var id = id_g + k
    var address1 = 'ws://' + host + ':9000' + "/?id=" + id

    var wsPort = new WebSocket()
    console.log("Searching port at 9000")
    wsPort.connect(address1, null)
    wsPort.on('connect', function (connection) {
        console.log(k2 + " connected to 9000");

        connection.on('message', function (message) {
            if (message.type === 'utf8') {
                console.log(k2 + " received: '" + message.utf8Data + "'");
                var address2 = 'ws://' + host + ':' + message.utf8Data + "/?id=" + id

                var ws = new WebSocket()
                console.log("Searching port at " + message.utf8Data)
                ws.connect(address2, null)
                ws.on('connect', function (connection2) {

                    var currentPos = { x: 1.0, y: 0 };

                    console.log(k2 + " connected to " + address2);

                    connection2.on('message', function (message) {
                        if (message.type === 'utf8') {
                            //    console.log(k2 + " received: '" + message.utf8Data + "'");

                            var obj = JSON.parse(message.utf8Data)
                            obj.forEach(e => {
                                if (e.cam != undefined) {

                                    currentPos.x = e.cam.x
                                    currentPos.y = e.cam.y
                                    //          console.log("cam found :" + e.cam.x + " " + e.cam.y)
                                }
                            })


                        }
                    });

                    var mouseSpeed = { x: 1.0, y: 0.0 }
                    var mousePosition = { x: 0, y: 0 }
                    var sendCommand = () => {

                        mouseSpeed.x = (mouseSpeed.x * 0.9 + (Math.random() - 0.5) * 100)
                        mouseSpeed.y = (mouseSpeed.y * 0.9 + (Math.random() - 0.5) * 100)
                        mousePosition.x += mouseSpeed.x
                        mousePosition.y += mouseSpeed.y

                        if (mousePosition.x < 100) mouseSpeed.x =Math. abs(mouseSpeed.x)
                        if (mousePosition.y < 100) mouseSpeed.y = Math.abs(mouseSpeed.y)
                        if (mousePosition.x > 1000) mouseSpeed.x = -Math.abs(mouseSpeed.x)
                        if (mousePosition.y > 1000) mouseSpeed.y = -Math.abs(mouseSpeed.y)


                        var toServer
                        if (currentPos.x != undefined)
                            toServer = JSON.stringify([{ hosts: [[currentPos.x * 1.0, currentPos.y * 1.0]], data: JSON.stringify(mousePosition) }])
                        else
                            toServer = JSON.stringify([{ hosts: [[]], data: "" }])
                        //    console.log(k2 + " Sending :\n" + toServer)

                        setTimeout(sendCommand, 33.3)
                        connection2.sendUTF(toServer)
                    }
                    setTimeout(sendCommand, 1000)



                })

            }
        });
    })



    // wsPort.onmessage = function (event) {
    //     data = event.data
    // console.log("Connection to : "+'ws://' + host +':'+data +"/?id="+id)
    //     ws = new WebSocket( )
    //     ws.onmessage = function (event) {
    //         frames++ 
    //         dataManipulationFunction(event.data)
    //     }
    // }


}


createOne()