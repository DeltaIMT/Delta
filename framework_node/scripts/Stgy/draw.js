var selectionSquare = null
var moveSquare = null
var swordmen = []
var bowmen = []
var coms = []
var arrows = []
var flags = []
var other = null
var trail = []
var selectedIds = []
var t = 0
var currentPos = { x: 0, y: 0 }
var cam = {}
var ping = 0

var low = false
document.addEventListener('keydown', function (event) {
    if (event.keyCode == 76) {
        low = !low
        console.log("low : " + low)
    }
});

module.exports.getPos = () => currentPos
module.exports.getCamera = () => cam
module.exports.setSelectionSquare = (drag) => selectionSquare = drag
module.exports.setSelectedId = (ids) => selectedIds = ids
module.exports.setMoveSquare = (drag) => moveSquare = drag
module.exports.setTrail = (newTrail) => trail = newTrail
var frameGetter = null
module.exports.setFrameGetter = (f) => frameGetter = f
module.exports.setPing = (newPing) => { ping = newPing }
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

            bowmen = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'bowman'
            }).map(k => frame[k])

            swordmen = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'swordman'
            }).map(k => frame[k])

            coms = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'com'
            }).map(k => frame[k])

            arrows = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'arrow'
            }).map(k => frame[k])

            flags = Object.keys(frame).filter(k => {
                return typeof (frame[k].type) !== undefined && frame[k].type == 'flag'
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

    var canvasCacheSelection = document.createElement('canvas')
    canvasCacheSelection.setAttribute('width', 80)
    canvasCacheSelection.setAttribute('height', 80)
    var contextSelection = canvasCacheSelection.getContext('2d')
    contextSelection.beginPath()
    contextSelection.arc(40, 40, 30, 0, Math.PI * 2)
    contextSelection.lineWidth = 2 * (2 + Math.sin(t * 0.10))
    contextSelection.strokeStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + "," + 0.7 + ")"
    contextSelection.stroke()

    //Selection Circle
    for (const id of selectedIds) {
        const bowman = frame[id]
        if (bowman !== undefined)
            context.drawImage(canvasCacheSelection, bowman.x - 40, bowman.y - 40)
    }

    if (!low) {
        var canvasCacheBowman = document.createElement('canvas')
        canvasCacheBowman.setAttribute('width', 80)
        canvasCacheBowman.setAttribute('height', 80)
        var contextBowman = canvasCacheBowman.getContext('2d')
        contextBowman.beginPath()
        contextBowman.arc(40, 40, 20, 0, Math.PI * 2)
        contextBowman.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + ",0.5)"
        contextBowman.shadowBlur = 30;
        contextBowman.shadowColor = "black";
        contextBowman.fill()
        contextBowman.shadowBlur = 20;
        contextBowman.fill()
        contextBowman.shadowBlur = 5;
        contextBowman.fill()
        contextBowman.shadowBlur = 3;
        contextBowman.fill()
        contextBowman.shadowBlur = 2;
        contextBowman.fill()


        var canvasCacheSwordman = document.createElement('canvas')
        canvasCacheSwordman.setAttribute('width', 80)
        canvasCacheSwordman.setAttribute('height', 80)
        var contextSwordman = canvasCacheSwordman.getContext('2d')
        contextSwordman.beginPath()
        var x = parseInt(40)
        var y = parseInt(40)
        contextSwordman.moveTo(x, y + 18);
        contextSwordman.lineTo(x + 20, y - 10);
        contextSwordman.lineTo(x - 20, y - 10);
        contextSwordman.closePath()
        contextSwordman.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + ",0.5)"
        contextSwordman.shadowBlur = 30;
        contextSwordman.shadowColor = "black";
        contextSwordman.fill()
        contextSwordman.shadowBlur = 20;
        contextSwordman.fill()
        contextSwordman.shadowBlur = 5;
        contextSwordman.fill()
        contextSwordman.shadowBlur = 3;
        contextSwordman.fill()
        contextSwordman.shadowBlur = 2;
        contextSwordman.fill()

        var canvasCacheCom = document.createElement('canvas')
        canvasCacheCom.setAttribute('width', 80)
        canvasCacheCom.setAttribute('height', 80)
        var contextCom = canvasCacheCom.getContext('2d')
        contextCom.beginPath()
        contextCom.arc(40, 40, 25, 0, Math.PI * 2)
        contextCom.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
        contextCom.shadowBlur = 30;
        contextCom.shadowColor = "black";
        contextCom.fill()

    }
    //Unit


    for (const bowman of bowmen) {
        if (!low)
            context.drawImage(canvasCacheBowman, bowman.x - 40, bowman.y - 40)
        context.beginPath()
        context.arc(bowman.x, bowman.y, 20, 0, Math.PI * 2)
        context.fillStyle = "rgb(" + parseInt(bowman.color[0]*bowman.health) + ", " + parseInt(bowman.color[1]*bowman.health) + ", " + parseInt(bowman.color[2]*bowman.health) + ")"
        context.fill()
    }

    for (const swordman of swordmen) {
        if (!low)
            context.drawImage(canvasCacheSwordman, swordman.x - 40, swordman.y - 40)
        context.beginPath()
        var x = parseInt(swordman.x)
        var y = parseInt(swordman.y)
        context.moveTo(x, y + 18);
        context.lineTo(x + 20, y - 10);
        context.lineTo(x - 20, y - 10);
        context.closePath()

        context.fillStyle = "rgb(" + parseInt(swordman.color[0]*swordman.health) + ", " + parseInt(swordman.color[1]*swordman.health) + ", " + parseInt(swordman.color[2]*swordman.health) + ")"
        context.fill()
    }

    for (const com of coms) {
        if (!low)
            context.drawImage(canvasCacheCom, com.x - 40, com.y - 40)
        context.beginPath()
        roundRect(context, com.x - 22, com.y - 22, 44, 44, 7)
        //context.rect(com.x - 25, com.y - 25, 50, 50)
        //  context.arc(com.x, com.y, 25, 0, Math.PI * 2)
        context.fillStyle = "rgb(" + parseInt(com.color[0]*com.health) + ", " + parseInt(com.color[1]*com.health) + ", " + parseInt(com.color[2]*com.health) + ")"
        context.fill()
    }



    if (!low) {
        var canvasArrowCache = document.createElement('canvas')
        canvasArrowCache.setAttribute('width', 40)
        canvasArrowCache.setAttribute('height', 40)
        var contextArrow = canvasArrowCache.getContext('2d')
        contextArrow.beginPath()
        contextArrow.arc(20, 20, 3, 0, Math.PI * 2)
        contextArrow.fillStyle = "rgb(" + 255 + ", " + 255 + ", " + 255 + ")"
        contextArrow.shadowBlur = 25;
        contextArrow.shadowColor = "rgb(" + 255 + ", " + 0 + ", " + 0 + ")"
        contextArrow.fill()
        contextArrow.shadowBlur = 15;
        contextArrow.shadowColor = "rgb(" + 255 + ", " + 200 + ", " + 100 + ")"
        contextArrow.fill()
        contextArrow.shadowBlur = 7;
        contextArrow.shadowColor = "white";
        contextArrow.fill()
        contextArrow.shadowBlur = 3;
        contextArrow.shadowColor = "white";
        contextArrow.fill()
    }
    for (const arrow of arrows) {
        if (!low)
            context.drawImage(canvasArrowCache, arrow.x - 20, arrow.y - 20)
        else {
            context.beginPath()
            context.arc(arrow.x, arrow.y, 3, 0, Math.PI * 2)
            context.fillStyle = "rgb(" + 255 + ", " + 255 + ", " + 255 + ")"
            context.fill()
        }
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

        context.beginPath()
        context.arc(flag.x, flag.y, 200, 0, Math.PI * 2)
        context.fillStyle = "rgba(" + flag.color[0] + ", " + flag.color[1] + ", " + flag.color[2] + "," + 0.1 + ")"
        context.fill()
    }


    // //Heath Bar
    // for (const bowman of bowmen) {
    //     context.beginPath()
    //     context.arc(bowman.x, bowman.y, 7, 0, Math.PI * 2)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 50 + ", " + 50 + ", " + 50 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(bowman.x, bowman.y, 7, 0, Math.PI * 2 * bowman.health)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 0 + ", " + 255 + ", " + 0 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(bowman.x, bowman.y, 12, 0, Math.PI * 2)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 50 + ", " + 50 + ", " + 50 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(bowman.x, bowman.y, 12, 0, Math.PI * 2 * (1.0 - 1.0 / (1.0 + 0.1 * parseFloat(bowman.xp))))
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 255 + ", " + 255 + ", " + 0 + ")"
    //     context.stroke()
    // }

    // //Heath Bar
    // for (const com of coms) {

    //     context.beginPath()
    //     context.arc(com.x, com.y, 7, 0, Math.PI * 2)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(com.x, com.y, 7, 0, Math.PI * 2 * com.health / 5.0)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 0 + ", " + 255 + ", " + 0 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(com.x, com.y, 12, 0, Math.PI * 2)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(com.x, com.y, 12, 0, Math.PI * 2 * com.spawning)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 50 + ", " + 130 + ", " + 255 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(com.x, com.y, 17, 0, Math.PI * 2)
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 50 + ", " + 50 + ", " + 50 + ")"
    //     context.stroke()

    //     context.beginPath()
    //     context.arc(com.x, com.y, 17, 0, Math.PI * 2 * (1.0 - 1.0 / (1.0 + 0.1 * parseFloat(com.xp))))
    //     context.lineWidth = 5
    //     context.strokeStyle = "rgb(" + 255 + ", " + 255 + ", " + 0 + ")"
    //     context.stroke()
    // }

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

    if (trail.length > 1) {
        context.shadowBlur = 30;
        context.beginPath()
        context.moveTo(trail[0].x, trail[0].y)
        for (const t of trail) {
            context.lineTo(t.x, t.y)
        }
        context.lineWidth = 2 * (2 + Math.sin(t * 0.10))
        context.strokeStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + "," + 0.5 + ")"
        context.stroke()
        context.shadowBlur = 0

    }
    context.shadowBlur = 30;
    context.shadowColor = "black"
    context.beginPath()
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
    context.rect(0, 0, 350, 130)
    context.fill()
    context.shadowBlur = 0;
    context.font = "bold 18px Courier New";
    context.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    if (other !== null)
        context.fillText("xp : " + other.xp + " used : " + other.usedXp, 10, 30)
    if (bowmen != undefined && flags != undefined)
        context.fillText("Total displayed : " + (bowmen.length + flags.length), 10, 55)
    context.fillText("Ping : " + parseInt(ping * 10) / 10.0, 10, 80)
    context.fillText("Draw fps        : " + parseInt(10000.0 / msElapsed) / 10.0, 10, 105)

    //Draw icon o spawning
    const iconX = 20
    const iconY = 150
    const iconSize = 60
    const iconHalfX = iconX + (iconSize / 2)



    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
    roundRect(context, iconX, iconY + 0 * (iconSize + iconSize / 3), iconSize, iconSize, 7)
    context.fill()
    roundRect(context, iconX, iconY + 1 * (iconSize + iconSize / 3), iconSize, iconSize, 7)
    context.fill()
    roundRect(context, iconX, iconY + 2 * (iconSize + iconSize / 3), iconSize, iconSize, 7)
    context.fill()
    context.beginPath()
    context.arc(iconHalfX, iconY + iconSize / 2, 20, 0, Math.PI * 2)
    const x3 = parseInt(iconHalfX)
    const y3 = parseInt(iconY + iconSize + iconSize / 3 + iconSize / 2)
    context.moveTo(x3, y3 + 18)
    context.lineTo(x3 + 20, y3 - 10)
    context.lineTo(x3 - 20, y3 - 10)
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 1.0 + ")"
    context.fill()
    context.closePath()
    roundRect(context, iconX + (iconSize - 44) / 2, iconY + 2 * (iconSize + iconSize / 3) + (iconSize - 44) / 2, 44, 44, 7)
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 1.0 + ")"
    context.fill()

    context.beginPath()
    context.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    context.fillText("1", iconHalfX-5,5+ iconY+ iconSize / 2)
    context.fillText("2", iconHalfX-5,5+ iconY + 1 * (iconSize + iconSize / 3)+ iconSize / 2)
    context.fillText("3", iconHalfX-5,5+ iconY + 2 * (iconSize + iconSize / 3)+ iconSize / 2)

}

