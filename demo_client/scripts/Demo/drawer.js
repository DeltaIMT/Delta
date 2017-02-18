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

    let balls = []
    let other = {}


    if ((frame) !== undefined) {
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


    drawBalls(balls)


}





const drawBalls = (balls) => {
    for (const ball of balls) {
        context.beginPath()
        context.arc(ball.x, ball.y, 4, 0, Math.PI * 2)
        const color = ball.color
        context.fillStyle = "rgba(" + color[0] + ", " + color[1] + ", " + color[2] + ",1)"
        context.fill()
    }
}
