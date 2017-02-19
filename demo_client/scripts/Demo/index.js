const client = require('./../../../framework/src/main/node/deltaClient')
const Drawer = require('./drawer')
const zlib = require('zlib')

let frame = null
let mouseDown = null
let mouseMove = null
window.onload = () => {
    const loop = () => {

        Drawer.draw(JSON.parse(JSON.stringify(frame)), mouseDown, mouseMove)
        window.requestAnimationFrame(loop)
    }
    window.requestAnimationFrame(loop)
}

client.launch()
client.dataManipulation(dataZiped => {
    let data = zlib.gunzip(Buffer.from(dataZiped, 'base64'), (err, data) => {
        //console.log("Received :\n" + data)
        frame = JSON.parse(data)
    })
})


document.addEventListener('mousedown', (event) => {
    mouseDown = { x: event.pageX, y: event.pageY }
})

document.addEventListener('mousemove', (event) => {
    mouseMove = { x: event.pageX, y: event.pageY }
})

document.addEventListener('mouseup', (event) => {
    const mouseUp = { x: event.pageX, y: event.pageY }
    const toTarget = { x: -mouseDown.x + mouseUp.x, y: -mouseDown.y + mouseUp.y }
    const length = Math.sqrt(toTarget.x * toTarget.x + toTarget.y * toTarget.y) * 0.3
    const dir = { x: parseFloat(toTarget.x / length), y: parseFloat(toTarget.y / length) }
    client.send(JSON.stringify([{
        hosts: JSON.stringify([parseInt(mouseDown.x), parseInt(mouseDown.y)]),
        data: JSON.stringify({ x: mouseDown.x, y: mouseDown.y, tx: dir.x, ty: dir.y })
    }]))
    console.log(JSON.stringify({ x: mouseDown.x, y: mouseDown.y, tx: dir.x, ty: dir.y }, null, 1))
    mouseDown = null
    mouseMove = null
})
