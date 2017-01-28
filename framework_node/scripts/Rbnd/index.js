var Mous = require('./mouse')
var Draw = require('./draw')
var client = require('../providedCode')
client.launch()

const frameInterp = require('./frameInterp')
Draw.setFrameGetter(frameInterp)



Mous.onDragEnd(drag => {
    var cam = Draw.getCamera()
    client.send(JSON.stringify([{

        hosts: [[Draw.getCurrentPos().x, Draw.getCurrentPos().y]],
        data: JSON.stringify({
            x: parseInt(drag.p1.x)+cam.x,
            y: parseInt(drag.p1.y)+cam.y,
            x2: parseInt(drag.p2.x)+cam.x,
            y2: parseInt(drag.p2.y)+cam.y
        })
    }]))
})

var zlib = require('zlib')
client.dataManipulation(dataZiped => {
    // console.log("Received Zipped :\n" + dataZiped)
    var data = zlib.gunzip(Buffer.from(dataZiped, 'base64'), (err, data) => {
        console.log("Received :\n" + data)
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

