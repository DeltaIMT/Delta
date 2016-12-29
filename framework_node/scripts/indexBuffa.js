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
    var clicked = false
    var rclicked = false

    document.addEventListener('mousemove', function (mouseMoveEvent) {
        mousePosition.x = mouseMoveEvent.pageX
        mousePosition.y = mouseMoveEvent.pageY
    }, false)

    document.addEventListener("click", function () {
        clicked = true
        setTimeout(() => clicked = false, 33)
    })

    document.addEventListener("contextmenu", function (e) {
        e.preventDefault()
        rclicked = true
        setTimeout(() => rclicked = false, 33)
    })


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
            if (blob.grap != undefined) {
                var dx = parseInt(blob.grap.x)
                var dy = parseInt(blob.grap.y)
                var ox = parseInt(blob.x)
                var oy = parseInt(blob.y)
                context.beginPath()
                context.moveTo(ox, oy);
                context.lineTo(ox + dx, oy + dy);
                context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
                context.stroke()
            }
        }

        for (var i = 0; i < scene.buffa.length; i++) {
            var buffa = scene.buffa[i]
            context.beginPath()
            context.arc(buffa.x, buffa.y, 60, 0, Math.PI * 2)
            context.fillStyle = "rgb(" + buffa.c[0] + ", " + buffa.c[1] + ", " + buffa.c[2] + ")"
            context.fill()
            context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
            context.stroke()
        }

        window.requestAnimationFrame(draw)
    }

    window.requestAnimationFrame(draw)


    var showFps = () => 
    {
        setTimeout(showFps,1000)
        console.log(client.countFps()) 
    }
    showFps()

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }

    client.commandToServer(() => {
        var toServer
        if (currentPos.x != undefined)
            toServer = JSON.stringify([{ hosts: [[currentPos.x * 1.0, currentPos.y * 1.0]], data: JSON.stringify({ x: mousePosition.x + cam.x, y: mousePosition.y + cam.y, cl: clicked, cr: rclicked }) }])
        else
            toServer = JSON.stringify([{ hosts: [[]], data: "" }])
        // console.log("Sending :\n" + toServer)
        return toServer
    })

    var scene = { blobs: [], buffa: [], grap: [] }
    client.dataManipulation(dataZiped => {
        //   console.log("Received Zipped :\n" + dataZiped)
        var data = zlib.gunzipSync(new Buffer(dataZiped, 'base64'))
        //   var data = dataZiped
        scene.blobs = []
        scene.buffa = []
        //  console.log("Received :\n" + data)
        var obj = JSON.parse(data)
        obj.forEach(e => {
            if (e.cam != undefined) {

                currentPos.x =  currentPos.x*0.95 + 0.05*e.cam.x
                currentPos.y =currentPos.y*0.95 + 0.05*e.cam.y
                //        console.log("cam found :" + cam.x + " " + cam.y)
            }
            else {
                if (e.t == 'p')
                    scene.blobs.push(e)
                else if (e.t == 'b')
                    scene.buffa.push(e)
            }

        })
        //  console.log(data)
    })
}
