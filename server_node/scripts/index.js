window.onload = function()
{
    var canvas = document.getElementById("canvas");
    if(!canvas)
    {
        alert("Impossible de récupérer le canvas");
        return;
    }

    var context = canvas.getContext("2d");
    if(!context)
    {
        alert("Impossible de récupérer le context");
        return;
    }

    context.canvas.width  = window.innerWidth;
    context.canvas.height = window.innerHeight;

    var worldSize = {x: 2000,y: 2000};
 
    var Snake = require('./Snake')
    var snakes = []

    var jsonData = []
    var id = require('./App')
    var host = window.location.hostname
    var frames = 0
    var ws = new WebSocket('ws://' + host + ':8080'+"/?id="+id );
          ws.onmessage = function (event) {
            frames++
          // console.log( event.data)
            jsonData = JSON.parse(event.data) 
            for (var i = 0; i<jsonData.length; i++) {
              var exists = false;
              var j = 0;
              while (!exists && j<snakes.length) {
                if (snakes[j].is(jsonData[i])) {
                  exists = true;
                  snakes[j].l = jsonData[i].l;
                  snakes[j].add(jsonData[i].x, jsonData[i].y);
                }
              }
              if (!exists) {
                snakes.push(new Snake(jsonData[i].id, jsonData[i].l, jsonData[i].x, jsonData[i].y))
              }
            }     
          };

    var mousePosition = {x:0, y:0};

    document.addEventListener('mousemove', function(mouseMoveEvent){   
        mousePosition.x = mouseMoveEvent.pageX;
        mousePosition.y = mouseMoveEvent.pageY;
    }, false);   
    
    var sendCommand = () =>
    {
    setTimeout(sendCommand,16.667)
    //console.log(mousePosition)
    var command = { "mouse" : mousePosition }
    ws.send( JSON.stringify(command)  )
    }
    setTimeout(sendCommand,1000)
    
    
    var countFPS = () =>
    {
      setTimeout(countFPS,1000)
      console.log("fps : " +frames)
      frames = 0
      console.log(JSON.stringify(jsonData,1));
    }
    setTimeout(countFPS,1000)
    
    
    var draw = () =>
    {
      context.clearRect(0, 0, canvas.width, canvas.height);
      context.beginPath();

      for (var i=0; i<snakes.length; i++) {
        context.arc(snakes[i].x, snakes[i].y, snakes[i].l*10 /*the radius*/, 0, Math.PI*2);
        context.strokeStyle = "#6D071A";
        context.stroke();
        context.fillStyle = "#A5260A";
        context.fill();
      }
    
    window.requestAnimationFrame(draw);
    }
    
    window.requestAnimationFrame(draw);

}