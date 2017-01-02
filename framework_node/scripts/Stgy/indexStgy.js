var Draw = require('./draw')
var Mous = require('./mouse')
var Intr = require('./intersection')

var moveOrder = []

Mous.onDragEnd((Mous) => {
    Draw.setSelectionSquare(null)
    selectedBowmen = bowmenVal.filter(b => b.h == true)
})

Mous.onDrag((Mous) => {
    Draw.setSelectionSquare(Mous)
    bowmenVal.forEach(bowman => {
        if (bowman.mine && Intr.pointInsideSquare(bowman, Mous))
            bowmen[bowman.id].h = true
        else
            bowmen[bowman.id].h = false
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

    // console.log(JSON.stringify(square, null, 1))
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
    //    console.log(JSON.stringify(square, null, 1))
    var offX = 0
    var offY = 0
    selectedBowmen.forEach(b => {

        var hosts = []
        for (var j = -1; j <= 1; j++)
            for (var i = -1; i <= 1; i++) {
                hosts.push([b.x * 1.0 + 600 * i, b.y * 1.0 + 600 * j])
            }


        moveOrder.push(
            {
                hosts: [[b.x * 1.0, b.y * 1.0]],
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

var makeId = () => Math.random().toString(36).substring(7)
var selectedBowmen = []
var bowmen = {}
var arrows = {}
var arrowsVal = []
var bowmenVal = []

const loop = () => {
    setTimeout(loop, 16.666)
    bowmenVal = Object.keys(bowmen).map(key => bowmen[key]);
    arrowsVal = Object.keys(arrows).map(key => arrows[key]);





    Draw.setBowmen(bowmenVal)
    Draw.setArrows(arrowsVal)
}
setTimeout(loop, 200)
var client = require('../providedCode')
client.launch()
var zlib = require('zlib')
client.dataManipulation(dataZiped => {
    // console.log("Received Zipped :\n" + dataZiped)
    var data = zlib.gunzip(Buffer.from(dataZiped, 'base64'), (err, data) => {
        //    console.log("Received :\n" + data)
        data = JSON.parse(data)
        data.forEach(e => {
            if (e.type == "bowman") {
                if (bowmen[e.id] == undefined)
                    bowmen[e.id] = {}
                Object.assign(bowmen[e.id], e)
                if (bowmen[e.id].h == undefined)
                    bowmen[e.id].h = false
                bowmen[e.id].counter = 5
            }
            else if (e.type == "arrow") {
                if (arrows[e.id] == undefined)
                    arrows[e.id] = {}
                Object.assign(arrows[e.id], e)
                arrows[e.id].counter = 5
            }
            else if (e.type == "camera") {
            }
        })

        arrowsVal.forEach(a => {
            a.counter--
            if (a.counter == 0)
                delete arrows[a.id]
        })

        bowmenVal.forEach(a => {
            a.counter--
            if (a.counter == 0) {
                console.log("DELETING BOW")
                delete bowmen[a.id]
            }

        })

    })
})

client.commandToServer(() => {
    var toServer
    if (moveOrder.length == 0)
        toServer = JSON.stringify([{ hosts: [], data: "" }])
    else
        toServer = JSON.stringify(moveOrder)
    moveOrder = []
    //  console.log("Sending :\n" + toServer)
    return toServer
})