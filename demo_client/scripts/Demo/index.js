
const client = require('./../../../framework/src/main/node/deltaClient')
const Drawer = require('./drawer')
const zlib = require('zlib')

let frame = null
let camera = {x:0,y:0}
window.onload = () => {
    const loop = () => {
        if(frame !=null)
        Drawer.draw(JSON.parse(JSON.stringify(frame)),camera)
        window.requestAnimationFrame(loop)
    }
    window.requestAnimationFrame(loop)
}

client.launch()
client.dataManipulation(dataZiped => {
    let data = zlib.gunzip(Buffer.from(dataZiped, 'base64'), (err, data) => {
        //console.log("Received :\n" + data)
        frame = JSON.parse(data)
        frame.forEach( e => {
            if( e.camera== true )  
           camera= e 
    })
    })
})


document.addEventListener('mousedown', (event) => {
    const mouseX = event.pageX
    const mouseY = event.pageY
    if (event.button == 0) {
        client.send(JSON.stringify([{
            hosts: JSON.stringify([parseInt(camera.x) - 100, parseInt(camera.y) - 100, 200, 200]),
            data: JSON.stringify({x:mouseX+parseInt(camera.x-window.innerWidth/2), y: mouseY+parseInt(camera.y-window.innerHeight/2)})
        }]
        ))
    }
}
)

document.addEventListener("contextmenu", function (e) {
    e.preventDefault()
})