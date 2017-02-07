window.onload = () => {
    var client = require('../providedCode')
    client.launch()
    var zlib = require('zlib');

    var worldSize = { x: 3000, y: 3000 }
    var cam = { x: 0, y: 0 }

    var canvas = document.getElementById("canvas")

    var background = new Image()
    background.src = 'texture/background.png' // TODO to change

    if (!canvas) {
        alert("Impossible de récupérer le canvas")
    }

    var context = canvas.getContext("2d")
    if (!context) {
        alert("Impossible de récupérer le context")
    }

    canvas.width = window.innerWidth
    canvas.height = window.innerHeight

    var drawings = {}

    var mousePosition = { x: 0, y: 0 }
    var leftClick = false
    var rightClick = false

    var uuidLine = require('node-uuid')
    var lineId = uuidLine.v4()
    var order = -1
    var color = [255, 0, 179]
    var thickness = 10

    document.addEventListener('mousemove', function (mouseMoveEvent) {
        mousePosition.x = mouseMoveEvent.pageX
        mousePosition.y = mouseMoveEvent.pageY

        if (rightClick) {
            cam.x = clamp(cam.x - mouseMoveEvent.movementX, 0, worldSize.x - canvas.width)
            cam.y = clamp(cam.y - mouseMoveEvent.movementY, 0, worldSize.y - canvas.height)
        }

        var toServer
        if (leftClick) {
            order++
            toServer = [{ hosts: JSON.stringify([mousePosition.x + cam.x, mousePosition.y + cam.y]), data: JSON.stringify({ mousePos: { x: mousePosition.x + cam.x, y: mousePosition.y + cam.y }, lineId: lineId, order: order, c: color, t: thickness }) }]
            //  console.log("Sending :\n" + toServer)
            client.send(JSON.stringify(toServer))
        }


    }, false)

    document.addEventListener("mousedown", function (e) {
        if (e.button == 0) {
            leftClick = true
        }
        if (e.button == 2) {
            rightClick = true
        }
    })

    document.addEventListener("contextmenu", function (e) {
        e.preventDefault()
    })

    document.addEventListener("mouseup", function () {
        leftClick = false;
        lineId = uuidLine.v4()
        rightClick = false;
    })

    var draw = () => {
        context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.translate(-cam.x, -cam.y);

        // draw the background
        var pattern = context.createPattern(background, 'repeat')
        context.beginPath()
        context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
        context.fillStyle = pattern
        context.fill()
        context.lineWidth = 5
        context.strokeStyle = "rgb(0,0,0)"

        if (drawings !== undefined && drawings.length > 0) {
            let lines = {}
            for (let point of drawings) {
                if (typeof (point.lineId) !== undefined) {

                    if (typeof (lines[point.lineId]) !== undefined && lines[point.lineId] !== undefined) 
                        lines[point.lineId].push(point)
                    else
                        lines[point.lineId] = [point]
                }
            }

            for (let key of Object.keys(lines)) {
                let line = lines[key].sort((a, b) => parseInt(a.order) - parseInt(b.order))
                context.beginPath()
                context.moveTo(line[0].x, line[0].y)
                for (let point of line.splice(1)) {
                    context.lineTo(point.x, point.y)
                }
                context.stroke()
            }
        }

        window.requestAnimationFrame(draw)
    }

    window.requestAnimationFrame(draw)

    /*var showFps = () => 
    {
        setTimeout(showFps,1000)
        console.log(client.countFps()) 
    }
    showFps()*/

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }

    client.dataManipulation(dataZiped => {
        //   console.log("Received Zipped :\n" + dataZiped)
        zlib.gunzip(Buffer.from(dataZiped, 'base64'), (err, data) => {
            drawings = JSON.parse(data)
            //console.log("Data : " + data)
            //console.log("Drawings = JSON.parse(data) : " + drawings)
        })
    })
}
