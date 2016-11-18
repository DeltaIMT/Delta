var uuid = require('node-uuid');
var k =0
var id = uuid.v4()
var createOne= () => {
k++
var WebSocketClient = require('websocket').client;
var client = new WebSocketClient();
 

var id2 = id +"/"+k
 
client.on('connect', function(connection) {
    console.log('WebSocket Client '+id2 +' Connected');
   
    // connection.on('message', function(message) {
    //     if (message.type === 'utf8') {
    //         console.log("Received: '" + message.utf8Data + "'");
    //     }
    // });

    var sendCommand = () =>
    {
    setTimeout(sendCommand,33.33333)
    var mousePosition = { x: Math.random()*6000 , y:Math.random()*6000}
    var command = { "mouse" : mousePosition }
    connection.send( JSON.stringify(command)  )
    }
    setTimeout(sendCommand,1000)
    
    
});
 
client.connect('ws://localhost:8080'+"/?id="+id2, null);
if(k <200) setTimeout(createOne,40)
}


createOne()