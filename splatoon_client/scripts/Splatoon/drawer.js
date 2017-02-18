const canvas = document.getElementById("canvas")
if (!canvas) {
    alert("Impossible de récupérer le canvas")
}
const context = canvas.getContext("2d")
if (!context) {
    alert("Impossible de récupérer le context")
}
canvas.width = window.innerWidth
canvas.height = window.innerHeight

const worldSize = { x: 3000, y: 3000 }
function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}
const background = new Image()
background.src = './texture/background.png'

const cam = {}
let currentPos = {}
module.exports.cam = () => cam
module.exports.pos = () => currentPos


module.exports.draw = (frame, msElapsed, time, ping, knownCases, mapCases, extendMiniMap) => {

    let players = []
    let balls = []
    let other = {}
    let player


    if ((frame) !== undefined) {
        players = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'shooter'
        }).map(k => frame[k])

        player = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'shooter' && frame[k].mine == true
        }).map(k => frame[k])

        balls = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'ball'
        }).map(k => frame[k])

        if (frame['1'] != undefined)
            other = frame['1']

        if (frame['0'] !== undefined)
            currentPos = frame['0']
    }


    // clamp the camera position to the world bounds while centering the camera around the snake                    
    cam.x = parseInt(clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width))
    cam.y = parseInt(clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height))
    context.setTransform(1, 0, 0, 1, 0, 0);  // because the transform matrix is cumulative

    context.translate(-cam.x, -cam.y);
    const pattern = context.createPattern(background, 'repeat')
    context.beginPath()
    context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y);
    context.fillStyle = pattern
    context.fill()


    drawCases(knownCases)
    drawPlayers(players, canvasCachePlayer)
    drawBalls(balls)


    context.translate(cam.x, cam.y);
    context.shadowBlur = 30;
    context.shadowColor = "black"
    context.beginPath()
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
    context.rect(0, 0, 350, 130)
    context.fill()
    context.shadowBlur = 0;
    context.font = "bold 18px Courier New";
    context.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    context.fillText("Ping : " + parseInt(ping * 10) / 10.0, 10, 80)
    context.fillText("Draw fps        : " + parseInt(10000.0 / msElapsed) / 10.0, 10, 105)
    context.closePath()

    drawMiniMap(mapCases, extendMiniMap, player)

}


const createCachePlayer = () => {
    const canvasCachePlayer = document.createElement('canvas')
    canvasCachePlayer.setAttribute('width', 80)
    canvasCachePlayer.setAttribute('height', 80)
    const contextPlayer = canvasCachePlayer.getContext('2d')
    contextPlayer.beginPath()
    contextPlayer.arc(40, 40, 20, 0, Math.PI * 2)
    contextPlayer.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + ",0.5)"
    contextPlayer.shadowBlur = 30;
    contextPlayer.shadowColor = "black";
    contextPlayer.fill()
    contextPlayer.shadowBlur = 20;
    contextPlayer.fill()
    contextPlayer.shadowBlur = 5;
    contextPlayer.fill()
    contextPlayer.shadowBlur = 3;
    contextPlayer.fill()
    contextPlayer.shadowBlur = 2;
    contextPlayer.fill()
    return canvasCachePlayer
}

const drawCases = (knownCases) => {
    for (let x = Math.round((cam.x) / 150) * 150; x < cam.x + canvas.width; x += 150)
        for (let y = Math.round((cam.y) / 150) * 150; y < cam.y + canvas.height; y += 150) {
            //console.log("is " +parseInt(x) + "/" + parseInt(y) +" known ?")
            if (knownCases[parseInt(x) + "/" + parseInt(y)] !== undefined) {

                let c = knownCases[parseInt(x) + "/" + parseInt(y)]
                //     console.log(c)
                context.beginPath()
                const color = parseInt(c.color)
                // console.log(color)
                const red = color == 0 ? 255 : 0
                const green = color == 1 ? 255 : 0
                const blue = color == 2 ? 255 : 0
                const transparency = color == 2 ? 0.2 : 0.5
                context.fillStyle = "rgba(" + red + ", " + green + ", " + blue + ", " + transparency + ")"
                context.rect(x, y, 150, 150)
                context.fill()
            }
        }
}

const drawMiniMap = (mapCases, extendMiniMap, player) => {
    //console.log(player)
    let cornerX
    let cornerY
    let mapSize
    let caseSize
    let caseRatio

    if(extendMiniMap == true) {
        cornerX = (canvas.width - 600)/2
        cornerY = (canvas.height - 600)/2
        mapSize = 600
        caseSize = 30
        caseRatio = 5
    }
    else {
        cornerX = canvas.width - 300
        cornerY = canvas.height - 300
        mapSize = 300
        caseSize = 15
        caseRatio = 10
    }

    context.beginPath()
    context.rect(cornerX,cornerY, mapSize, mapSize)
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
    context.fill()
    context.closePath()

    for(const p of player){
        context.beginPath()
        context.arc(cornerX + parseInt(p.x)/caseRatio, cornerY + parseInt(p.y)/caseRatio, 20/caseRatio, 0, Math.PI*2)
        context.fillStyle = "rgb(" + 255 + ", " + 255 + ", " + 0 + ")"
        context.fill()
        context.closePath()
    }


    for(let x = 0; x < worldSize.x; x += 150) {
        for(let y = 0; y < worldSize.y; y += 150) {
            if (mapCases[x + "/" + y] !== undefined) {
                let c = mapCases[parseInt(x) + "/" + parseInt(y)]
                let mx = parseInt(c.x)/caseRatio
                let my = parseInt(c.y)/caseRatio
                let color = parseInt(c.color)
                const red = color == 0 ? 255 : 0
                const green = color == 1 ? 255 : 0
                const blue = color == 2 ? 255 : 0
                context.beginPath()
                context.rect(cornerX + mx,cornerY + my, caseSize, caseSize)
                context.fillStyle = "rgba(" + red + ", " + green + ", " + blue + "," + 0.4 + ")"
                context.fill()
                context.closePath()
            }
        }
    }
}

const drawPlayers = (players, canvasCachePlayer) => {
    for (const player of players) {
        context.drawImage(canvasCachePlayer, player.x - 40, player.y - 40)
        const hit = player.hit
        const color = player.color
        //console.log(hit)
        if (hit==1){
            context.beginPath()
            context.arc(player.x, player.y, 23, 0, Math.PI * 2)
            if (color == 0) {
                context.fillStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + ",1)"
            }
            else {
                context.fillStyle = "rgba(" + 255 + ", " + 0 + ", " + 0 + ",1)"
            }
            context.fill()
            context.closePath()
        }
        context.beginPath()
        context.arc(player.x, player.y, 20, 0, Math.PI * 2)
        const red = color == 0 ? 255 : 0
        const green = color == 1 ? 255 : 0
        const blue = color == 2 ? 255 : 0
        context.fillStyle = "rgba(" + red + ", " + green + ", " + blue + ",1)"
        context.fill()
        context.closePath()
    }
}

const drawBalls = (balls) => {
    for (const ball of balls) {
        //console.log(ball)
        context.beginPath()
        context.arc(ball.x, ball.y, 4, 0, Math.PI * 2)
        const color = ball.color
        const red = color == 0 ? 255 : 0
        const green = color == 1 ? 255 : 0
        const blue = color == 2 ? 255 : 0
        context.fillStyle = "rgba(" + red + ", " + green + ", " + blue + ",1)"
        context.fill()
    }
}


const canvasCachePlayer = createCachePlayer()