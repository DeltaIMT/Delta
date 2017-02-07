const frameInterp = require('./frameInterp')
const Drawer = require('./drawer')
const client = require('../providedCode')
const zlib = require('zlib')


let centerPosition = null

let ping = 0
window.onload = () => {
    let time = 0
    let endTime = performance.now()
    let msElapsed = 0
    let startTime = performance.now()
    let trail = []
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
    //    console.log("Received :\n" + data)
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

document.addEventListener("contextmenu", function (e) {
    e.preventDefault()
})

document.addEventListener('mousedown', function (e) {
    let cam = Drawer.cam()
    const mouse = { x: cam.x + e.pageX, y: cam.y + e.pageY }
    const hosts = [parseInt(centerPosition.x*1.0- 10), parseInt(centerPosition.y*1.0 -10) , 20,20]

    if (e.button == 0) {
        client.send(
            JSON.stringify([{
                hosts: JSON.stringify(hosts),
                data: JSON.stringify({
                    x: mouse.x,
                    y: mouse.y
                })
            }])
        )
    }
    else if (e.button == 2) {
        client.send(
            JSON.stringify([{
                hosts: JSON.stringify(hosts),
                data: JSON.stringify({
                    b: true
                })
            }])
        )
    }
})

document.addEventListener('mouseup', function (e) {
    let cam = Drawer.cam()
    const mouse = { x: cam.x + e.pageX, y: cam.y + e.pageY }
    const hosts = [parseInt(centerPosition.x*1.0- 10), parseInt(centerPosition.y*1.0 -10) , 20,20]
  
    if (e.button == 2) {
        client.send(
            JSON.stringify([{
                hosts:  JSON.stringify(hosts),
                data: JSON.stringify({
                    b: false
                })
            }])
        )
    }
})