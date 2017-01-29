const frameInterp = require('./frameInterp')
const Drawer = require('./drawer')
const client = require('../providedCode')
const zlib = require('zlib')
const Mous = require('./mouse')
const Intr = require('./intersection')


let selectedIds = []
let selectionSquare = null

let selectable = []
let centerPosition = null
window.onload = () => {
    let time = 0

    let ping = 0
    let endTime = performance.now()
    let msElapsed = 0
    let startTime = performance.now()

    let moveSquare = null

    let trail = []


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

        Drawer.draw(frame, msElapsed, time, ping, selectedIds, moveSquare, selectionSquare, trail)
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
    if (keyName === 49 || keyName === 50 || keyName === 51) {
        client.send(
            JSON.stringify([{
                hosts: [[centerPosition.x, centerPosition.y]],
                data: JSON.stringify({
                    id: "" + (parseInt(keyName) - 48),
                    x: centerPosition.x,
                    y: centerPosition.y
                })
            }])
        )

    }
}
)
