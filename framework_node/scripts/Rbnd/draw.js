var frameGetter = {}
var balls = []
var walls = []
var other = null
var t = 0
var currentPos = { x: 0, y: 0 }
var cam = {}
var ping = 0

module.exports.getCamera = () => cam
module.exports.setFrameGetter = (f) => frameGetter = f
module.exports.setPing = (newPing) => { ping = newPing }
module.exports.getCurrentPos = () => currentPos


var startTime = performance.now()
var endTime
var msElapsed = 16.6
window.onload = () => {
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

    var draw = () => {
        endTime = performance.now()
        msElapsed = msElapsed * 0.90 + (endTime - startTime) * 0.1
        startTime = endTime
        t++
        drawer(context)
        window.requestAnimationFrame(draw)
    }
    window.requestAnimationFrame(draw)
}


var worldSize = { x: 3000, y: 3000 }
function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}

var background = new Image()
background.src = './texture/background.png'

function roundRect(ctx, x, y, width, height, radius) {
    if (typeof radius === 'number') {
        radius = { tl: radius, tr: radius, br: radius, bl: radius };
    } else {
        var defaultRadius = { tl: 0, tr: 0, br: 0, bl: 0 };
        for (var side in defaultRadius) {
            radius[side] = radius[side] || defaultRadius[side];
        }
    }
    ctx.beginPath();
    ctx.moveTo(x + radius.tl, y);
    ctx.lineTo(x + width - radius.tr, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius.tr);
    ctx.lineTo(x + width, y + height - radius.br);
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius.br, y + height);
    ctx.lineTo(x + radius.bl, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius.bl);
    ctx.lineTo(x, y + radius.tl);
    ctx.quadraticCurveTo(x, y, x + radius.tl, y);
    ctx.closePath();

}


const drawer = (context) => {

    let frame = {}
    if (frameGetter != null) {
        frame = frameGetter.getInterp()
        if ((frame) !== undefined) {

            balls = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'ball'
            }).map(k => frame[k])

            walls = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'wall'
            }).map(k => frame[k])
            if (frame['1'] != undefined)
                other = frame['1']

            if (frame['0'] !== undefined)
                currentPos = frame['0']
        }
    }


    // clamp the camera position to the world bounds while centering the camera around the snake                    
    cam.x = parseInt(clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width))
    cam.y = parseInt(clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height))
    context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative
    //  context.clearRect(0, 0, canvas.width, canvas.height);
    context.translate(-cam.x, -cam.y);
    var pattern = context.createPattern(background, 'repeat')
    context.beginPath()
    context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
    context.fillStyle = pattern
    context.fill()

    for (const ball of balls) {
        context.beginPath()
        context.arc(ball.x, ball.y, 20, 0, Math.PI * 2)
        context.fillStyle = "rgb(255,255,255)"
        context.fill()
    }

    for (const wall of walls) {
        context.beginPath()
        context.moveTo(wall.x, wall.y)
        context.lineTo(wall.x2, wall.y2)
        context.strokeStyle = "rgb(" + 0 + ", " + 255 + ", " + 255 + ")"
        context.lineWidth = 4
        context.stroke()
        context.closePath()
    }

    context.shadowBlur = 30;
    context.shadowColor = "black";
    context.beginPath()
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
    context.rect(0, 0, 350, 130)
    context.fill()
    context.shadowBlur = 0;
    context.font = "bold 18px Courier New";
    context.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    if (other !== null)
        context.fillText("Own units       : " + other.n, 10, 30);

    context.fillText("Ping : " + parseInt(ping * 10) / 10.0, 10, 80);
    context.fillText("Draw fps        : " + parseInt(10000.0 / msElapsed) / 10.0, 10, 105);
}

