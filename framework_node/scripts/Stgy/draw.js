var selectionSquare = null
var moveSquare = null
var swordmen = []
var bowmen = []
var flags = []
var t = 0
module.exports.setSelectionSquare = (drag) => selectionSquare = drag
module.exports.setBowmen = (newBowmen) => bowmen = newBowmen
module.exports.setMoveSquare = (drag) => moveSquare = drag
window.onload = () => {

    var currentPos = { x: 0, y: 0 }
    var worldSize = { x: 3000, y: 3000 }

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
    // draw the background
    var background = new Image()
    background.src = './texture/background.png'


    var draw = () => {
        t++
        var cam = {}
        // clamp the camera position to the world bounds while centering the camera around the snake                    
        cam.x = clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width);
        cam.y = clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height);

        context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.translate(-cam.x, -cam.y);
    var pattern = context.createPattern(background, 'repeat')
        context.beginPath()
        context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
        context.fillStyle = pattern
        context.fill()

        //console.log("Drawing UNIT ___________ " +bowmen.length )
        //Unit
        for (const bowman of bowmen) {
            //     console.log("draw  : "+ JSON.stringify(bowman,null,1))
            context.beginPath()
            context.arc(bowman.x, bowman.y, 20, 0, Math.PI * 2)
            context.fillStyle = "rgb(" + bowman.color[0] + ", " + bowman.color[1] + ", " + bowman.color[2] + ")"
            context.lineWidth = 1
            context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
            context.fill()
        }
        //Selection Circle
        for (const bowman of bowmen) {
            if (bowman.h == true) {
                context.beginPath()
                context.arc(bowman.x, bowman.y, 30, 0, Math.PI * 2)
                context.lineWidth = 2 * (2 + Math.sin(t * 0.10))
                context.strokeStyle = "rgba(" + bowman.color[0] + ", " + bowman.color[1] + ", " + bowman.color[2] + "," + 0.5 + ")"
                context.stroke()
            }
        }
        //Heath Bar
        for (const bowman of bowmen) {

            context.beginPath()
            context.rect(bowman.x - 25, bowman.y - 20, 50, 5)
            context.fillStyle = "rgb(" + 50 + ", " + 50 + ", " + 50 + ")"
            context.fill()

            context.beginPath()
            context.rect(bowman.x - 25, bowman.y - 20, 50 * bowman.health, 5)
            context.fillStyle = "rgb(" + 0 + ", " + 255 + ", " + 0 + ")"
            context.fill()
        }


        if (moveSquare) {
            context.beginPath()
            var x1 = moveSquare.x1 + cam.x
            var y1 = moveSquare.y1 + cam.y
            var x2 = moveSquare.x2 + cam.x
            var y2 = moveSquare.y2 + cam.y
            context.moveTo(x1, y1);
            context.lineTo(x2, y2);
            var px = moveSquare.px * moveSquare.numL
            var py = moveSquare.py * moveSquare.numL
            context.lineTo(x2 + px, y2 + py);
            context.lineTo(x1 + px, y1 + py);
            context.closePath()
            context.fillStyle = "rgba(" + 200 + ", " + 255 + ", " + 200 + "," + 0.5 + ")"
            context.fill()
        }

        if (selectionSquare) {
            context.beginPath()
            context.rect(selectionSquare.p1.x + cam.x, selectionSquare.p1.y + cam.y, selectionSquare.p2.x - selectionSquare.p1.x, selectionSquare.p2.y - selectionSquare.p1.y)
            context.fillStyle = "rgba(" + 200 + ", " + 255 + ", " + 255 + "," + 0.3 + ")"
            context.lineWidth = 1
            context.fill()
        }

        window.requestAnimationFrame(draw)
    }

    window.requestAnimationFrame(draw)

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }
}
