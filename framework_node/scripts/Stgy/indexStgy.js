var Draw = require('./draw')
var Mous = require('./mouse')
var Intr = require('./intersection')

var moveOrder = []
var getTranslatedMouse = (mouse) => {
    var cam = Draw.getCamera()
    var trans = {}
    trans.p1 = { x: mouse.p1.x * 1 + cam.x * 1, y: mouse.p1.y * 1 + cam.y * 1 }
    trans.p2 = { x: mouse.p2.x * 1 + cam.x * 1, y: mouse.p2.y * 1 + cam.y * 1 }
    return trans
}

Mous.onDragEnd((mouse) => {
    Draw.setSelectionSquare(null)
    selectedBowmen = bowmenVal.filter(b => b.h == true)
})

Mous.onDrag((mouse) => {
    //  console.log("BEFORE : " + JSON.stringify(mouse, null, 1))
    mouse = getTranslatedMouse(mouse)
    //  console.log("AFTER : " + JSON.stringify(mouse, null, 1))
    Draw.setSelectionSquare(mouse)
    bowmenVal.forEach(bowman => {
        if (bowman.mine && Intr.pointInsideSquare(bowman, mouse))
            bowmen[bowman.id].h = true
        else
            bowmen[bowman.id].h = false
    })
})

var moveSquareCompute = (mouse) => {
    var num = selectedBowmen.length
    var x1 = mouse.p1.x
    var y1 = mouse.p1.y
    var x2 = mouse.p2.x
    var y2 = mouse.p2.y
    var vx = x2 - x1
    var vy = y2 - y1
    if (Math.abs(vx) > 30 || Math.abs(vy)>30 ) {
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

    else {
        var square = {}
        var sqrt = Math.sqrt(num)
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
    var square = moveSquareCompute(getTranslatedMouse(mouse))

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
                hosts: hosts,
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

Mous.onDragRight((mouse) => {
    var square = moveSquareCompute(getTranslatedMouse(mouse))
    Draw.setMoveSquare(square)
})

Mous.onTrail((trail) => {
    Draw.setTrail(trail)
})


var linearInterp = (path, float_i) => {
    var first = path[Math.floor(float_i)]
    var last = path[Math.ceil(float_i)]
    var t = float_i - Math.floor(float_i)
    return { x: first.x * (1 - t) + last.x * t, y: first.y * (1 - t) + last.y * t }
}

Mous.onTrailEnd((trail) => {
    Draw.setTrail([])

    var trailSize = trail.length
    var selectedSize = selectedBowmen.length
    var cam = Draw.getCamera()
    for (var i = 0; i < selectedSize; i++) {
        const b = selectedBowmen[i]
        const pos = linearInterp(trail, parseFloat(i * trailSize) / parseFloat(selectedSize))
        pos.x += cam.x
        pos.y += cam.y
        moveOrder.push(
            {
                hosts: [[b.x * 1.0, b.y * 1.0]],
                data: JSON.stringify({
                    id: b.id,
                    x: parseInt(pos.x),
                    y: parseInt(pos.y)
                })
            }
        )
    }


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
var flags = {}
var flagsVal = []

const loop = () => {
    setTimeout(loop, 16.666)
    bowmenVal = Object.keys(bowmen).map(key => bowmen[key])
    arrowsVal = Object.keys(arrows).map(key => arrows[key])
    flagsVal = Object.keys(flags).map(key => flags[key])
    Draw.setBowmen(bowmenVal)
    Draw.setArrows(arrowsVal)
    Draw.setFlags(flagsVal)
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
                    bowmen[e.id] = {h:false}
                Object.assign(bowmen[e.id], e)
                bowmen[e.id].counter = 5
            }
            else if (e.type == "arrow") {
                if (arrows[e.id] == undefined)
                    arrows[e.id] = {}
                Object.assign(arrows[e.id], e)
                arrows[e.id].counter = 5
            }
            else if (e.type == "flag") {
                if (flags[e.id] == undefined)
                    flags[e.id] = {}
                Object.assign(flags[e.id], e)
                flags[e.id].counter = 5
            }
            else if (e.type == "camera") {
                Draw.setCamera(e)
            }
            else if (e.type == "other") {
                Draw.setOther(e)
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
                delete bowmen[a.id]
            }
        })

        flagsVal.forEach(a => {
            a.counter--
            if (a.counter == 0) {
                delete flags[a.id]
            }
        })

    })
})

client.commandToServer(() => {
    var toServer
    if (moveOrder.length == 0)
        toServer = JSON.stringify([{ hosts: [], data: "" }])
    else {

        if (moveOrder.length > 30) {
            toServer = JSON.stringify(moveOrder.slice(0, 30))
            moveOrder = moveOrder.slice(30);
        }
        else {
            toServer = JSON.stringify(moveOrder)
            moveOrder = []
        }
        //console.log("Sending :\n" + toServer)
    }

      //console.log("Sending :\n" + toServer)
    return toServer
})