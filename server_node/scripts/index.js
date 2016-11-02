window.onload = function()
{
    var canvas = document.getElementById("canvas")
    if(!canvas)
    {
        alert("Impossible de récupérer le canvas")
        return
    }

    var context = canvas.getContext("2d")
    if(!context)
    {
        alert("Impossible de récupérer le context")
        return
    }

    context.canvas.width  = window.innerWidth
    context.canvas.height = window.innerHeight

    var worldSize = {x: 2000,y: 2000}
 
    var Snake = require('./Snake')
    var snakes = []

    var jsonData = []
    var id = require('./App')
    var host = window.location.hostname
    var frames = 0
    var ws = new WebSocket('ws://' + host + ':8080'+"/?id="+id )
          ws.onmessage = function (event) {
            frames++
          // console.log( event.data)
            jsonData = JSON.parse(event.data) 
            for (var i = 0; i<jsonData.length; i++) {
              var exists = false
              var data = jsonData[i]
              var j = 0
              while (!exists && j<snakes.length) {
                var snake = snakes[j]
                if (snake.is(data.id)) {
                  exists = true
                  snake.l = data.l
                  snake.r = data.r
                  // snake.color = data.color
                  snake.add(data.x, data.y)
                }
                j++
              }
              if (!exists) {
                snakes.push(new Snake(data.id, data.x, data.y, data.r, data.l, data.rgb))
              }
            }
            //TODO delete snakes when dead    
          }

    var mousePosition = {x:0, y:0}

    document.addEventListener('mousemove', function(mouseMoveEvent){   
        mousePosition.x = mouseMoveEvent.pageX
        mousePosition.y = mouseMoveEvent.pageY
    }, false)   
    
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
      console.log(JSON.stringify(jsonData,1))
    }
    setTimeout(countFPS,1000)
    
    
    var draw = () =>
    {
      context.clearRect(0, 0, canvas.width, canvas.height)

      for (var i=0; i<snakes.length; i++) {
        var snake = snakes[i]
        for (var j=0; j<snake.positions.length; j++) {
          context.beginPath()
          context.arc(snake.positions[j].x, snake.positions[j].y, snake.r, 0, Math.PI*2)
          context.fillStyle = "rgb(" + snake.rgb[0] + ", " + snake.rgb[1] + ", " + snake.rgb[2] + ")"
          context.fill()
        }
      }
    
    window.requestAnimationFrame(draw)
    }
    
    window.requestAnimationFrame(draw)

}