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

    var worldSize = {x: 6000, y: 6000}
    var cam = {x: 0, y: 0}

    var currentSnake = {x: 0, y: 0} // position of the snake's head : the camera has to be centered around it 

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
              if (jsonData[i].id!= undefined)
              { 
              var data = jsonData[i]
              var j = 0
              while (!exists && j<snakes.length) {
                var snake = snakes[j]
                if (snake.is(data.id)) {
                  if (data.c) {
                    snakes.splice(j,1)
                  }
                  else {
                    exists = true
                    snake.l = data.l
                    snake.r = data.r
                    snake.rgb = data.rgb
                    
                    if (snake.is(id)) {
                      currentSnake.x = data.x
                      currentSnake.y = data.y
                    }
                    snake.add(data.x, data.y)  
                  }
                }
                j++
              }
              if (!exists && !data.c) {
                snakes.push(new Snake(data.id, data.x, data.y, data.r, data.l, data.rgb))
              }
            }
            }
          }

    var mousePosition = {x:0, y:0}

    document.addEventListener('mousemove', function(mouseMoveEvent){   
        mousePosition.x = mouseMoveEvent.pageX + cam.x
        mousePosition.y = mouseMoveEvent.pageY + cam.y
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
      // clamp the camera position to the world bounds while centering the camera around the snake                    
      cam.x = clamp(currentSnake.x - canvas.width/2, 0, worldSize.x - canvas.width);
      cam.y = clamp(currentSnake.y - canvas.height/2, 0, worldSize.y - canvas.height);

      context.setTransform(1,0,0,1,0,0);  // because the transform matrix is cumulative
      context.clearRect(0, 0, canvas.width, canvas.height);
      context.translate(-cam.x, -cam.y); 

      // draw the background
      var background = new Image()
      background.src = 'pictures/background.png'
      var pattern = context.createPattern(background, 'repeat')

      context.beginPath()
      context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
      context.fillStyle = pattern
      context.fill()

      // draw the snakes
      for (var i=0; i<snakes.length; i++) {
        var snake = snakes[i]
        for (var j=0; j<snake.positions.length; j++) {
          context.beginPath()
          context.arc(snake.positions[j].x, snake.positions[j].y, snake.r, 0, Math.PI*2)
          context.fillStyle = "rgb(" + snake.rgb[0] + ", " + snake.rgb[1] + ", " + snake.rgb[2] + ")"
          context.fill()
          context.strokeStyle = "rgb(" + snake.rgb[0]/2 + ", " + snake.rgb[1]/2 + ", " + snake.rgb[2]/2 + ")"
          context.stroke()
        }
      }
    
    window.requestAnimationFrame(draw)
    }
    
    window.requestAnimationFrame(draw)

    function clamp(value, min, max){
        return Math.min(Math.max(value, min), max);
    } 

}