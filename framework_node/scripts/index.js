window.onload = () => {
    var client = require('./providedCode')
    client.launch()
    var zlib = require('zlib');


    var currentPos = { x: 0, y: 0 }


    var canvas = document.getElementById("canvas")
    if (!canvas) {
        alert("Impossible de récupérer le canvas")
    }

    var context = canvas.getContext("2d")
    if (!context) {
        alert("Impossible de récupérer le context")
    }

    canvas.width = window.innerWidth
    canvas.height = window.innerHeight


    var worldSize = { x: 3000, y: 3000 }
    var cam = { x: 0, y: 0 }

    var mousePosition = { x: 0, y: 0 }

    document.addEventListener('mousemove', function (mouseMoveEvent) {
        mousePosition.x = mouseMoveEvent.pageX 
        mousePosition.y = mouseMoveEvent.pageY 
    }, false)



    var draw = () => {
        // clamp the camera position to the world bounds while centering the camera around the snake                    
        cam.x = clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width);
        cam.y = clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height);

        context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.translate(-cam.x, -cam.y);

        // draw the background
        var background = new Image()
        background.src = 'texture/background.png'
        var pattern = context.createPattern(background, 'repeat')

        context.beginPath()
        context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
        context.fillStyle = pattern
        context.fill()

        // draw the blobs
        for (var i = 0; i < scene.blobs.length; i++) {
            var blob = scene.blobs[i]
            context.beginPath()
            context.arc(blob.x, blob.y, 20, 0, Math.PI * 2)
            context.fillStyle = "rgb(" + blob.c[0] + ", " + blob.c[1] + ", " + blob.c[2] + ")"
            context.fill()
            context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
            context.stroke()
        }
        window.requestAnimationFrame(draw)
    }

    window.requestAnimationFrame(draw)

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }




    client.commandToServer(() => {
        var toServer
        if (currentPos.x != undefined)
        toServer  = JSON.stringify([{ hosts: [[currentPos.x*1.0, currentPos.y*1.0]], data: JSON.stringify({x: mousePosition.x  + cam.x,y:mousePosition.y+cam.y}) }])
        else 
        toServer = JSON.stringify([{ hosts: [[]], data: "" }])
        console.log("Sending :\n"+toServer)
        return toServer
    })


    var scene = { blobs: [] }

    client.dataManipulation(dataZiped => {
        
        var data = dataZiped//zlib.gunzipSync(dataZiped).toString('utf8');
        scene.blobs = []
        console.log("Received :\n"+data)
        var obj = JSON.parse(data)
        obj.forEach(e => {
            if (e.cam != undefined) {

                currentPos.x = e.cam.x
                currentPos.y = e.cam.y
        //        console.log("cam found :" + cam.x + " " + cam.y)
            }
            else
                scene.blobs.push(e)
        })
        //  console.log(data)
    })


}
