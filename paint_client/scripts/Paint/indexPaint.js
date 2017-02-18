window.onload = () => {
    document.getElementById("root").style.visibility = "visible"
    document.getElementById("root").style.display = "block"

    var client = require('../providedCode')
    client.launch()
    var zlib = require('zlib');

    var customSettings = require("./settingsPaint")

    var worldSize = { x: 3000, y: 3000 }
    var cam = { x: 0, y: 0 }

    var canvas = document.getElementById("canvas")

    var background = new Image()
    background.src = 'texture/backgroundPaint.png'

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
    var erasing = 0

    var order = -1
    var color = [255, 0, 179]
    var thickness = 12
    var cut = 0

    document.addEventListener('mousemove', function (mouseMoveEvent) {
        mousePosition.x = mouseMoveEvent.pageX
        mousePosition.y = mouseMoveEvent.pageY

        if (rightClick) {
            cam.x = clamp(cam.x - mouseMoveEvent.movementX, 0, worldSize.x - canvas.width)
            cam.y = clamp(cam.y - mouseMoveEvent.movementY, 0, worldSize.y - canvas.height)
        }

        var toServer
        if (leftClick) {
            if (customSettings.erasing !== undefined)
                erasing = customSettings.erasing
            if (customSettings.customColor !== undefined)
                color = customSettings.customColor
            if (customSettings.customThickness !== undefined)
                thickness = customSettings.customThickness

            console.log("Erasing : " + erasing)
            order++
            toServer = [{
                hosts: JSON.stringify([mousePosition.x + cam.x, mousePosition.y + cam.y]),
                data: JSON.stringify({
                    mousePos: { x: mousePosition.x + cam.x, y: mousePosition.y + cam.y }, order: order,
                    c: color, t: thickness, cut: cut, erase: erasing
                })
            }]
            cut = 0
            //console.log("Sending :\n" + toServer.data)
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
        cut = 1
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

        if (drawings !== undefined && drawings.length > 0) {
            // sort points by id; for each id, draw the points correctly (sort them by order then iterate and draw)
            let pointsByClient = {}
            for (let point of drawings) {
                if (typeof (point.id) !== undefined) {
                    if (typeof (pointsByClient[point.id]) !== undefined && pointsByClient[point.id] !== undefined)
                        pointsByClient[point.id].push(point)
                    else
                        pointsByClient[point.id] = [point]
                }
            }

            for (let id of Object.keys(pointsByClient)) {
                let pointsByOrder = pointsByClient[id].sort((a, b) => parseInt(a.order) - parseInt(b.order))
                context.lineCap = 'round'

                context.beginPath()
                context.moveTo(pointsByOrder[0].x, pointsByOrder[0].y)

                let i = 1
                while (i < pointsByOrder.length) {
                    let point = pointsByOrder[i]

                    if (point.cut == 0) {
                        context.lineWidth = point.t
                        context.strokeStyle = "rgb(" + point.c[0] + ", " + point.c[1] + ", " + point.c[2] + ")"
                        context.lineTo(point.x, point.y)
                        context.stroke()
                    }
                    else if (point.cut == 1) {
                        context.stroke()
                        context.closePath()

                        context.beginPath()
                        context.moveTo(point.x, point.y)
                    }
                    else if (point.cut == 2) {
                        context.lineTo(point.x, point.y)
                        context.stroke()
                        context.closePath()

                        let point2 = pointsByOrder[i + 1]
                        context.beginPath()
                        context.moveTo(point2.x, point2.y)
                        i++
                    }
                    i++
                }
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


