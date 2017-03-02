const canvas = document.getElementById("canvas")
const context = canvas.getContext("2d")
canvas.width = 600
canvas.height = 600

const background = new Image()
background.src = './texture/background.png'

module.exports.draw = (frame, mouseDown, mouseMove) => {

    const pattern = context.createPattern(background, 'repeat')
    context.beginPath()
    context.rect(0, 0, 600, 600)
    context.fillStyle = pattern
    context.fill()

    if (frame != null)
        frame.forEach(e => {
            drawBall(e)
        })

    if (mouseMove != null && mouseDown != null) {
        context.beginPath()
        context.moveTo(mouseDown.x, mouseDown.y)
        context.lineTo(mouseMove.x, mouseMove.y)
        context.strokeStyle = "green"
        context.lineWidth = 5
        context.stroke()
        context.closePath()
        const toTarget = { x: -mouseDown.x + mouseMove.x, y: -mouseDown.y + mouseMove.y }
        const length = Math.sqrt(toTarget.x * toTarget.x + toTarget.y * toTarget.y) * 0.2
        const dir = { x: parseFloat(toTarget.x / length), y: parseFloat(toTarget.y / length) }
        context.fillStyle = "black"
        context.font = "30px Calibri";
        context.fillText(" x:" + mouseDown.x + " y:" + mouseDown.y, mouseDown.x, mouseDown.y);
        context.fillText(" tx:" + parseInt(dir.x * 100) / 100 + " ty:" + parseInt(dir.y * 100) / 100, mouseMove.x, mouseMove.y);
    }
}

const drawBall = (ball) => {
    context.beginPath()
    context.arc(ball.x, ball.y, 20, 0, Math.PI * 2)
    context.fillStyle = "rgb(0,0,0)"
    context.fill()
}
