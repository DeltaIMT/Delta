const frameInterp = require('./frameInterp')
const Drawer = require('./drawer')
const client = require('../providedCode')
const zlib = require('zlib')
const Mous = require('./mouse')
const Intr = require('./intersection')


let selectedIds = []
let selectionSquare = null
let moveSquare = null
let selectable = []
let centerPosition = null
let moveOrder = []
let moveTrail = []
let ping = 0
window.onload = () => {
    let time = 0
    let endTime = performance.now()
    let msElapsed = 0
    let startTime = performance.now()

    const loop = () => {
        time += 1
        endTime = performance.now()
        msElapsed = msElapsed * 0.90 + (endTime - startTime) * 0.1
        startTime = endTime
        const frame = frameInterp.getInterp()
        centerPosition = frame['0']
        let newSelectable = []
        const selectableIds = Object.keys(frame).filter(k => { return frame[k].mine && (frame[k].type == "bowman" || frame[k].type == "com" || frame[k].type == "swordman") })
        selectableIds.forEach(k => { newSelectable.push(frame[k]) })
        selectable = newSelectable


        if (moveOrder.length > 0) {
            let toServer
            let numToSend = 20
            if (moveOrder.length > numToSend) {
                toServer = JSON.stringify(moveOrder.slice(0, numToSend))
                moveOrder = moveOrder.slice(numToSend);
            }
            else {
                toServer = JSON.stringify(moveOrder)
                moveOrder = []
            }
            client.send(toServer)
        }

        Drawer.draw(frame, msElapsed, time, ping, selectedIds, moveSquare, selectionSquare, moveTrail)
        window.requestAnimationFrame(loop)
    }
    window.requestAnimationFrame(loop)
}


client.launch()
client.dataManipulation(dataZiped => {
    let data = zlib.gunzip(Buffer.from(dataZiped, 'base64'), (err, data) => {
        //console.log("Received :\n" + data)
        data = JSON.parse(data)
        const newFrame = {}
        data.forEach(o => {
            if (typeof (o.id) !== undefined) {
                newFrame[o.id] = o
            }
        })
        frameInterp.addFrame(newFrame)
    })
})

setInterval(() => { client.getPing((ping1) => ping = ping1) }, 1000)

const getTranslatedMouse = (mouse) => {
    let cam = Drawer.cam()
    let trans = {}
    trans.p1 = { x: mouse.p1.x * 1 + cam.x * 1, y: mouse.p1.y * 1 + cam.y * 1 }
    trans.p2 = { x: mouse.p2.x * 1 + cam.x * 1, y: mouse.p2.y * 1 + cam.y * 1 }
    return trans
}

Mous.onDragEnd((mouse) => {
    selectionSquare = null

    let toBeSelected = []
    selectable.forEach(e => {
        if (e.mine && Intr.pointInsideSquare(e, getTranslatedMouse(mouse)))
            toBeSelected.push(e.id)
    })
    selectedIds = toBeSelected
})

Mous.onDrag((mouse) => {

    mouse = getTranslatedMouse(mouse)
    selectionSquare = mouse

})

document.addEventListener("contextmenu", function (e) {
    e.preventDefault()
})

document.addEventListener('keydown', (event) => {
    const keyName = event.keyCode;
    if (keyName === 49 || keyName === 50 || keyName === 51 || keyName === 49 + 7) {
        client.send(
            JSON.stringify([{
                hosts: JSON.stringify([-1]),
                data: JSON.stringify({
                    id: "" + (parseInt(keyName) - 48)
                })
            }])
        )
    }
}
)

const moveSquareCompute = (mouse) => {
    const num = selectedIds.length
    const x1 = mouse.p1.x
    const y1 = mouse.p1.y
    const x2 = mouse.p2.x
    const y2 = mouse.p2.y
    let vx = x2 - x1
    let vy = y2 - y1
    if (Math.abs(vx) > 30 || Math.abs(vy) > 30) {
        const vl = Math.sqrt(vx * vx + vy * vy)
        const numInWidth = (vl / 45)
        const numInLength = num / numInWidth
        vx /= vl; vy /= vl;
        let px = -vy
        let py = vx
        px *= 45
        py *= 45
        return { x1: x1, y1: y1, x2: x2, y2: y2, vx: vx * 45, vy: vy * 45, px: px, py: py, numW: numInWidth, numL: numInLength }
    }

    else {
        let square = {}
        let sqrt = Math.sqrt(num)
        square.numW = sqrt
        square.numL = sqrt
        square.x1 = x1 - square.numW * 45 / 2
        square.y1 = y1 - square.numL * 45 / 2
        square.x2 = x1 + square.numW * 45 / 2
        square.y2 = square.y1
        square.vx = 45
        square.vy = 0
        square.px = 0
        square.py = 45
        return square
    }

}

Mous.onDragRightEnd((mouse) => {
    const square = moveSquareCompute(getTranslatedMouse(mouse))

    let offX = 0
    let offY = 0
    selectedIds.forEach(k => {
        const b = selectable.find(e => e.id = k)
        const hosts = [parseInt(b.x - 250), parseInt(b.y - 250), 500, 500]
        

        moveOrder.push(
            {
                hosts: JSON.stringify(hosts),
                data: JSON.stringify({
                    id: b.id,
                    x: parseInt(square.x1 + offX * square.vx + offY * square.px),
                    y: parseInt(square.y1 + offX * square.vy + offY * square.py)
                })
            }
        )
        offX += 1
        if (offX >= square.numW) {
            offX = 0
            offY += 1
        }
    })
    moveSquare = null
})

Mous.onDragRight((mouse) => {
    const square = moveSquareCompute(getTranslatedMouse(mouse))
    moveSquare = square
})

Mous.onTrail((trail) => {
    moveTrail = trail
})

Mous.onTrailEnd((trail) => {
    moveTrail = []

    const trailSize = trail.length
    const selectedSize = selectedIds.length
    console.log(selectedSize)
     let cam = Drawer.cam()
    
    for (let i = 0; i < selectedSize; i++) {
         const b = selectable.find(e => e.id = selectedIds[i])
        const pos = linearInterp(trail, parseFloat(i * trailSize) / parseFloat(selectedSize))
        pos.x += cam.x
        pos.y += cam.y
        moveOrder.push(
            {
                hosts: JSON.stringify([parseInt(b.x - 250), parseInt(b.y - 250), 500, 500]),
                data: JSON.stringify({
                    id: b.id,
                    x: parseInt(pos.x),
                    y: parseInt(pos.y)
                })
            }
        )
    }

})

const linearInterp = (path, float_i) => {
    const first = path[Math.floor(float_i)]
    const last = path[Math.ceil(float_i)]
    const t = float_i - Math.floor(float_i)
    return { x: first.x * (1 - t) + last.x * t, y: first.y * (1 - t) + last.y * t }
}