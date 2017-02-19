var WebSocket = require('websocket').client;
var host = '192.168.43.4'
var uuid = require('node-uuid');
var id_g = uuid.v4()
var k = 0
var N = 200
var zlib = require('zlib');

var createOne = () => {
    var k2 = k
    k++
    if (k < N) {

        setTimeout(createOne, 200)
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

                    console.log(k2 + " connected to " + address2);
                  // connection2.close()
                   connection.close()

                })
            }
        });
    })
}
createOne()