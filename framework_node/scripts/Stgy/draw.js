var selectionSquare = null
var moveSquare = null
var swordmen = []
var bowmen = []
var arrows = []
var flags = []
var other = {}
var t = 0
var currentPos = { x: 0, y: 0 }
var cam = {}
module.exports.setCamera = (cam) => {
    currentPos.x = currentPos.x * 0.9 + cam.x * 0.1
    currentPos.y = currentPos.y * 0.9 + cam.y * 0.1
}
module.exports.getCamera = () => cam
module.exports.setSelectionSquare = (drag) => selectionSquare = drag
module.exports.setBowmen = (newBowmen) => bowmen = newBowmen
module.exports.setArrows = (newArrows) => arrows = newArrows
module.exports.setFlags = (newflags) => flags = newflags
module.exports.setOther = (newOther) => other = newOther
module.exports.setMoveSquare = (drag) => moveSquare = drag
window.onload = () => {


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
    // canvas.width = 800
    // canvas.height = 600
    // draw the background
    var background = new Image()
    background.src = './texture/background.png'

    var startTime = performance.now()
    var endTime
    var msElapsed = 16.6
    var draw = () => {
        endTime = performance.now()
        msElapsed = msElapsed * 0.90 + (endTime - startTime) * 0.1
        startTime = endTime
        t++
        // clamp the camera position to the world bounds while centering the camera around the snake                    
        cam.x = clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width);
        cam.y = clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height);

        context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative
        //  context.clearRect(0, 0, canvas.width, canvas.height);
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
            // context.shadowBlur = 30;
            // context.shadowColor = "black";
            context.fill()

            // for (var k = 1; k <= 3; k++) {
            //     context.beginPath()
            //     context.arc(bowman.x, bowman.y, 20, 0, Math.PI * 2)
            //     context.shadowBlur = k * k * 5;
            //     context.shadowColor = "black";
            //     context.fill()
            // }
            context.shadowBlur = 0;

        }

        for (const arrow of arrows) {
            context.beginPath()
            context.arc(arrow.x, arrow.y, 5, 0, Math.PI * 2)
            context.fillStyle = "rgb(" + arrow.color[0] + ", " + arrow.color[1] + ", " + arrow.color[2] + ")"
            context.fill()
        }

        for (const flag of flags) {
            context.beginPath()
            var x = parseInt(flag.x)
            var y = parseInt(flag.y)
            context.moveTo(x, y + 28);
            context.lineTo(x + 20, y);
            context.lineTo(x - 20, y);
            context.closePath()
            context.fillStyle = "rgb(" + flag.color[0] + ", " + flag.color[1] + ", " + flag.color[2] + ")"
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

        //Possessing and spawning Bar
        for (const flag of flags) {
            context.beginPath()
            context.rect(flag.x - 25, flag.y - 20, 50, 5)
            context.fillStyle = "rgb(" + 50 + ", " + 50 + ", " + 50 + ")"
            context.fill()

            context.beginPath()
            context.rect(flag.x - 25, flag.y - 20, 50 * flag.possessing, 5)
            context.fillStyle = "rgb(" + 50 + ", " + 130 + ", " + 255 + ")"
            context.fill()

            context.beginPath()
            context.rect(flag.x - 25, flag.y - 10, 50, 5)
            context.fillStyle = "rgb(" + 50 + ", " + 50 + ", " + 50 + ")"
            context.fill()

            context.beginPath()
            context.rect(flag.x - 25, flag.y - 10, 50 * flag.spawning, 5)
            context.fillStyle = "rgb(" + 200 + ", " + 255 + ", " + 0 + ")"
            context.fill()
        }

        if (moveSquare) {
            context.shadowBlur = 30;
            context.beginPath()
            var x1 = moveSquare.x1
            var y1 = moveSquare.y1
            var x2 = moveSquare.x2
            var y2 = moveSquare.y2
            context.moveTo(x1, y1);
            context.lineTo(x2, y2);
            var px = moveSquare.px * moveSquare.numL
            var py = moveSquare.py * moveSquare.numL
            context.lineTo(x2 + px, y2 + py);
            context.lineTo(x1 + px, y1 + py);
            context.closePath()
            context.fillStyle = "rgba(" + 200 + ", " + 255 + ", " + 200 + "," + 0.4 + ")"
            context.fill()
            context.shadowBlur = 0;
        }

        if (selectionSquare) {
            context.beginPath()
            context.shadowBlur = 30;
            context.rect(selectionSquare.p1.x, selectionSquare.p1.y, selectionSquare.p2.x - selectionSquare.p1.x, selectionSquare.p2.y - selectionSquare.p1.y)
            context.fillStyle = "rgba(" + 200 + ", " + 255 + ", " + 255 + "," + 0.4 + ")"
            context.lineWidth = 1
            context.fill()
            context.shadowBlur = 0;
        }
        context.translate(cam.x, cam.y);
        context.shadowBlur = 30;
        context.shadowColor = "black";
        context.beginPath()
        context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
        context.rect(0, 0, 350, 100)
        context.fill()
        context.shadowBlur = 0;
        context.font = "bold 18px Courier New";
        context.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
        context.fillText("Number of units : " + other.n, 10, 30);
        context.fillText("Draw ms elapsed : " + parseInt(msElapsed * 10) / 10.0, 10, 55);
        context.fillText("Draw fps        : " + parseInt(10000.0 / msElapsed) / 10.0, 10, 80);


        window.requestAnimationFrame(draw)
    }

    window.requestAnimationFrame(draw)

    function clamp(value, min, max) {
        return Math.min(Math.max(value, min), max);
    }
}
