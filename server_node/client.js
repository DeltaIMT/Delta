var uuid = require('node-uuid');
for(var k = 0; k < 40;k++)
{
var WebSocketClient = require('websocket').client;
var client = new WebSocketClient();
 

var id = k
 
client.on('connect', function(connection) {
    console.log('WebSocket Client '+id +' Connected');
   
    // connection.on('message', function(message) {
    //     if (message.type === 'utf8') {
    //         console.log("Received: '" + message.utf8Data + "'");
    //     }
    // });

    var sendCommand = () =>
    {
    setTimeout(sendCommand,16.667)
    var mousePosition = { x: Math.random()*800 , y:Math.random()*800}
    var command = { "mouse" : mousePosition }
    connection.send( JSON.stringify(command)  )
    }
    setTimeout(sendCommand,1000)
    
    
});
 
client.connect('ws://localhost:8080'+"/?id="+id, null);

}
