var WebSocket = require('websocket').client;
var host = '127.0.0.1'
var uuid = require('node-uuid');
var id_g = uuid.v4()
var k = 0
var N = 100

var createOne = () => {

k++
if(k < N)
setTimeout(createOne,100)

var id = id_g+k
var address1 = 'ws://' + host + ':9000'+"/?id="+id

var wsPort = new WebSocket()
console.log("Searching port at 9000")
wsPort.connect(address1,null)
wsPort.on('connect', function(connection) {
    console.log(k + " connected to 9000");
   
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
            console.log(k+ " received: '" + message.utf8Data + "'");
            var address2 = 'ws://' + host +':'+message.utf8Data +"/?id="+id

                    var ws = new WebSocket()
                    console.log("Searching port at " + message.utf8Data)
                    ws.connect(address2,null)
                    ws.on('connect', function(connection) {
                        console.log(k + " connected to " + address2);
                    
                        connection.on('message', function(message) {
                            if (message.type === 'utf8') {
                                console.log(k+ " received: '" + message.utf8Data + "'");
                            }
                        });
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