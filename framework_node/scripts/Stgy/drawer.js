
const canvas = document.getElementById("canvas")
if (!canvas) {
    alert("Impossible de récupérer le canvas")
}
const context = canvas.getContext("2d")
if (!context) {
    alert("Impossible de récupérer le context")
}
canvas.width = window.innerWidth
canvas.height = window.innerHeight

const worldSize = { x: 3000, y: 3000 }
function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}
const background = new Image()
background.src = './texture/background.png'

const cam = {}
module.exports.cam = () => cam




module.exports.draw = (frame, msElapsed, time, ping, selectedIds, moveSquare, selectionSquare, trail) => {

    let bowmen = []
    let swordmen = []
    let coms = []
    let arrows = []
    let other = {}
    let currentPos = {}
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

        if (frame['1'] != undefined)
            other = frame['1']

        if (frame['0'] !== undefined)
            currentPos = frame['0']
    }

    // clamp the camera position to the world bounds while centering the camera around the snake                    
    cam.x = parseInt(clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width))
    cam.y = parseInt(clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height))
    context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative

    context.translate(-cam.x, -cam.y);
    const pattern = context.createPattern(background, 'repeat')
    context.beginPath()
    context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
    context.fillStyle = pattern
    context.fill()

    const canvasCacheSelection = createCanvasCacheSelection(time)


    drawSelectionCircle(selectedIds, frame, canvasCacheSelection)
    drawBowmen(bowmen, canvasCacheBowman)
    drawSwordmen(swordmen, canvasCacheSwordman)
    drawComs(coms, canvasCacheCom)
    drawArrows(arrows, canvasArrowCache)

    if (moveSquare) drawMoveSquare(moveSquare)
    if (selectionSquare) drawSelectionSquare(selectionSquare)
    context.translate(cam.x, cam.y);
    if (trail.length > 1) drawTrail(trail,time)

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
    context.fillText("Units : " + other.n, 10, 50)
    context.fillText("Ping : " + parseInt(ping * 10) / 10.0, 10, 80)
    context.fillText("Draw fps        : " + parseInt(10000.0 / msElapsed) / 10.0, 10, 105)

    //Draw icon o spawning
    drawIcons()

}

const getColor = (e) => {
    if (typeof (e.color) !== undefined && (e.color) !== undefined && typeof (e.color[0]) !== undefined && typeof (e.color[1]) !== undefined && typeof (e.color[2]) !== undefined && typeof (e.health) !== undefined)
        return "rgb(" + parseInt(e.color[0] * e.health) + ", " + parseInt(e.color[1] * e.health) + ", " + parseInt(e.color[2] * e.health) + ")"
    else
        return "rgb(0,0,0)"
}

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

const createCanvasCacheSelection = (time) => {
    const canvasCacheSelection = document.createElement('canvas')
    canvasCacheSelection.setAttribute('width', 80)
    canvasCacheSelection.setAttribute('height', 80)
    const contextSelection = canvasCacheSelection.getContext('2d')
    contextSelection.beginPath()
    contextSelection.arc(40, 40, 30, 0, Math.PI * 2)
    contextSelection.lineWidth = 2 * (2 + Math.sin(time * 0.10))
    contextSelection.strokeStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + "," + 0.7 + ")"
    contextSelection.stroke()
    return canvasCacheSelection
}


const createCacheBowman = () => {
    const canvasCacheBowman = document.createElement('canvas')
    canvasCacheBowman.setAttribute('width', 80)
    canvasCacheBowman.setAttribute('height', 80)
    const contextBowman = canvasCacheBowman.getContext('2d')
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
    return canvasCacheBowman
}

const createCacheSwordman = () => {
    const canvasCacheSwordman = document.createElement('canvas')
    canvasCacheSwordman.setAttribute('width', 80)
    canvasCacheSwordman.setAttribute('height', 80)
    const contextSwordman = canvasCacheSwordman.getContext('2d')
    contextSwordman.beginPath()
    let x = parseInt(40)
    let y = parseInt(40)
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
    return canvasCacheSwordman
}

const createCacheCom = () => {
    const canvasCacheCom = document.createElement('canvas')
    canvasCacheCom.setAttribute('width', 80)
    canvasCacheCom.setAttribute('height', 80)
    const contextCom = canvasCacheCom.getContext('2d')
    contextCom.beginPath()
    contextCom.arc(40, 40, 25, 0, Math.PI * 2)
    contextCom.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    contextCom.shadowBlur = 30;
    contextCom.shadowColor = "black";
    contextCom.fill()
    return canvasCacheCom
}

const createCacheArrow = () => {
    const canvasArrowCache = document.createElement('canvas')
    canvasArrowCache.setAttribute('width', 40)
    canvasArrowCache.setAttribute('height', 40)
    const contextArrow = canvasArrowCache.getContext('2d')
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
    return canvasArrowCache
}


const drawSelectionCircle = (selectedIds, frame, canvasCacheSelection) => {
    for (const id of selectedIds) {
        const e = frame[id]
        if (e !== undefined)
            context.drawImage(canvasCacheSelection, e.x - 40, e.y - 40)
    }
}


const drawBowmen = (bowmen, canvasCacheBowman) => {
    for (const bowman of bowmen) {
        context.drawImage(canvasCacheBowman, bowman.x - 40, bowman.y - 40)
        context.beginPath()
        context.arc(bowman.x, bowman.y, 20, 0, Math.PI * 2)
        context.fillStyle = getColor(bowman)
        context.fill()
    }
}

const drawSwordmen = (swordmen, canvasCacheSwordman) => {

    for (const swordman of swordmen) {
        context.drawImage(canvasCacheSwordman, swordman.x - 40, swordman.y - 40)
        context.beginPath()
        const x = parseInt(swordman.x)
        const y = parseInt(swordman.y)
        context.moveTo(x, y + 18);
        context.lineTo(x + 20, y - 10);
        context.lineTo(x - 20, y - 10);
        context.closePath()

        context.fillStyle = getColor(swordman)
        context.fill()
    }
}

const drawComs = (coms, canvasCacheCom) => {
    for (const com of coms) {

        context.drawImage(canvasCacheCom, com.x - 40, com.y - 40)
        context.beginPath()
        roundRect(context, com.x - 22, com.y - 22, 44, 44, 7)
        context.fillStyle =getColor(com)
        context.fill()
    }
}

const drawArrows = (arrows, canvasArrowCache) => {
    for (const arrow of arrows) {
        context.drawImage(canvasArrowCache, arrow.x - 20, arrow.y - 20)
    }
}


const drawMoveSquare = (moveSquare) => {
    context.shadowBlur = 30;
    context.beginPath()
    let x1 = moveSquare.x1
    let y1 = moveSquare.y1
    let x2 = moveSquare.x2
    let y2 = moveSquare.y2
    context.moveTo(x1, y1);
    context.lineTo(x2, y2);
    let px = moveSquare.px * moveSquare.numL
    let py = moveSquare.py * moveSquare.numL
    context.lineTo(x2 + px, y2 + py);
    context.lineTo(x1 + px, y1 + py);
    context.closePath()
    context.fillStyle = "rgba(" + 200 + ", " + 255 + ", " + 200 + "," + 0.4 + ")"
    context.fill()
    context.shadowBlur = 0;
}

const drawSelectionSquare = (selectionSquare) => {
    context.beginPath()
    context.shadowBlur = 30;
    context.rect(selectionSquare.p1.x, selectionSquare.p1.y, selectionSquare.p2.x - selectionSquare.p1.x, selectionSquare.p2.y - selectionSquare.p1.y)
    context.fillStyle = "rgba(" + 200 + ", " + 255 + ", " + 255 + "," + 0.4 + ")"
    context.lineWidth = 1
    context.fill()
    context.shadowBlur = 0;
}

const drawTrail = (trail,time) => {
    context.shadowBlur = 30;
    context.beginPath()
    context.moveTo(trail[0].x, trail[0].y)
    for (const t of trail) {
        context.lineTo(t.x, t.y)
    }
    context.lineWidth = 2 * (2 + Math.sin(time * 0.10))
    context.strokeStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + "," + 0.5 + ")"
    context.stroke()
    context.shadowBlur = 0
}

const drawIcons = () => {
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
    context.fillText("1", iconHalfX - 5, 5 + iconY + iconSize / 2)
    context.fillText("2", iconHalfX - 5, 5 + iconY + 1 * (iconSize + iconSize / 3) + iconSize / 2)
    context.fillText("3", iconHalfX - 5, 5 + iconY + 2 * (iconSize + iconSize / 3) + iconSize / 2)
}


const canvasCacheBowman = createCacheBowman()
const canvasCacheSwordman = createCacheSwordman()
const canvasCacheCom = createCacheCom()
const canvasArrowCache = createCacheArrow()