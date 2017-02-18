const frameInterp = require('./frameInterp')
const client = require('./../../../framework/src/main/node/deltaClient')
const Drawer = require('./drawer')
const zlib = require('zlib')

let centerPosition = null
let ping = 0
let moveOrder = {}


let knownCases = {}
let mapCases = {}
let extendMiniMap = false



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
        Drawer.draw(frame, msElapsed, time, ping)
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

const keys = { 90: "z", 83: "s", 81: "q", 68: "d", 32: "space" }


document.addEventListener('keydown', (event) => {
    const keyName = event.keyCode;

    if (keyName == 32) {
        extendMiniMap = true
    }
    const cam = Drawer.pos()
    client.send(JSON.stringify([{
        hosts: JSON.stringify([parseInt(cam.x) - 100, parseInt(cam.y) - 100, 200, 200]),
        data: JSON.stringify({ keydown: keys[keyName] })
    }]
    ))
}
)

document.addEventListener('keyup', (event) => {
    const keyName = event.keyCode;

    if (keyName == 32) {
        extendMiniMap = false
    }
    
    const cam = Drawer.pos()
    client.send(JSON.stringify([{
        hosts: JSON.stringify([parseInt(cam.x) - 100, parseInt(cam.y) - 100, 200, 200]),
        data: JSON.stringify({ keyup: keys[keyName] })
    }]
    ))
}
)

document.addEventListener('mousedown', (event) => {
    const pos = Drawer.pos()
    const cam = Drawer.cam()
    const mouseX = event.pageX
    const mouseY = event.pageY

    if (event.button == 0) {
        client.send(JSON.stringify([{
            hosts: JSON.stringify([parseInt(pos.x) - 100, parseInt(pos.y) - 100, 200, 200]),
            data: JSON.stringify({x:mouseX+parseInt(cam.x), y: mouseY+parseInt(cam.y)})
        }]
        ))
    }
    else if (event.button == 2) {
        const toX = ((mouseX - (window.innerWidth-600)/2)/600)*3000
        const toY = ((mouseY - (window.innerHeight-600)/2)/600)*3000
        client.send(JSON.stringify([{
            hosts: JSON.stringify([parseInt(pos.x) - 100, parseInt(pos.y) - 100, 200, 200]),
            data: JSON.stringify({tx:parseInt(toX), ty:parseInt(toY)})
        }]
        ))
    }
}
)

document.addEventListener("contextmenu", function (e) {
    e.preventDefault()
})