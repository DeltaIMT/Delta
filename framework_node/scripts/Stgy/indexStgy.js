var Draw = require('./draw')
var Mous = require('./mouse')
var Intr = require('./intersection')

Mous.onDragEnd((Mous) => {
    Draw.setSelectionSquare(null)
    selectedBowmen = bowmen.filter(b => b.h == true)
})

Mous.onDrag((Mous) => {
    Draw.setSelectionSquare(Mous)
    bowmen = bowmen.map(bowman => {
        if (bowman.mine && Intr.pointInsideSquare(bowman, Mous))
            bowman.h = true
        else
            bowman.h = false
        return bowman
    })
})

var moveSquareCompute = (Mous) => {
    var num = selectedBowmen.length
    var x1 = Mous.p1.x
    var y1 = Mous.p1.y
    var x2 = Mous.p2.x
    var y2 = Mous.p2.y
    var vx = x2 - x1
    var vy = y2 - y1
    var vl = Math.sqrt(vx * vx + vy * vy)
    var numInWidth = (vl / 45)
    var numInLength = num / numInWidth
    vx /= vl; vy /= vl;
    var px = -vy
    var py = vx
    px *= 45
    py *= 45
    return { x1: x1, y1: y1, x2: x2, y2: y2, vx: vx * 45, vy: vy * 45, px: px, py: py, numW: numInWidth, numL: numInLength }
}

Mous.onDragRightEnd((Mous) => {
    var square = moveSquareCompute(Mous)

    console.log(JSON.stringify(square, null, 1))
    if (!square.vx) {
        var sqrt = Math.sqrt(selectedBowmen.length)
        square.numW = sqrt
        square.numL = sqrt
        square.x1 -= square.numW * 45 / 2
        square.y1 -= square.numL * 45 / 2
        square.vx = 45
        square.vy = 0
        square.px = 0
        square.py = 45
    }
    console.log(JSON.stringify(square, null, 1))
    var offX = 0
    var offY = 0
    selectedBowmen.forEach(b => {
        b.tx = square.x1 + offX * square.vx + offY * square.px
        b.ty = square.y1 + offX * square.vy + offY * square.py
        offX += 1
        if (offX >= square.numW) {
            offX = 0
            offY += 1
        }
    })
    //selectedBowmen go to position 
    Draw.setMoveSquare(null)
})


Mous.onDragRight((Mous) => {
    var square = moveSquareCompute(Mous)
    Draw.setMoveSquare(square)
})

document.addEventListener("contextmenu", function (e) {
    e.preventDefault()
});

var makeId = () => { Math.random().toString(36).substring(7) }
var selectedBowmen = []
var bowmen = new Array(100).join().split(',').map(() => {
    var team = parseInt(Math.random() * 8)
    var color = []
    if (team == 0)
        color = [255, 0, 0]
    else if (team == 1)
        color = [255, 255, 0]
    else if (team == 2)
        color = [0, 255, 0]
    else if (team == 3)
        color = [0, 255, 255]
    else if (team == 4)
        color = [0, 0, 255]
    else if (team == 5)
        color = [255, 0, 255]
    else if (team == 6)
        color = [123, 0, 0]
    else if (team == 7)
        color = [0, 123, 0]
    else
        color = [0, 0, 123]
    return {
        id: makeId(),
        mine: team == 4,
        color: color,
        health: Math.random(),
        x: Math.random() * 1000.0,
        y: Math.random() * 1000,
        tx: null,
        ty: null,
        h: false
    }
})


const loop = () => {
    setTimeout(loop, 16.666)

    bowmen.forEach(bowman => {
        if (bowman.tx != null) {
            var x = Math.sign(bowman.tx - bowman.x)
            var y = Math.sign(bowman.ty - bowman.y)
            bowman.x += x * 1
            bowman.y += y * 1
        }
        else {
            bowman.x += Math.random() * 6 - 3; bowman.y += Math.random() * 6 - 3
        }
    })


    Draw.setBowmen(bowmen)

}
setTimeout(loop, 200)

var client = require('../providedCode')
client.launch()
var zlib = require('zlib')


client.dataManipulation(dataZiped => {
    //   console.log("Received Zipped :\n" + dataZiped)
    var data = zlib.gunzipSync(new Buffer(dataZiped, 'base64'))
    console.log("Received :\n" + data)

})

client.commandToServer(() => {
    var toServer
    toServer = JSON.stringify([{ hosts: [], data: "" }])
    // console.log("Sending :\n" + toServer)
    return toServer
})