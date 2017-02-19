const canvas = document.getElementById("canvas")
const context = canvas.getContext("2d")
canvas.width = window.innerWidth
canvas.height = window.innerHeight



module.exports.draw = (frame, camera) => {
    context.setTransform(1, 0, 0, 1, 0, 0)

    context.beginPath()
    context.rect(0, 0, canvas.width, canvas.height)
    context.fillStyle = "rgb(255,255,255)"
    context.fill()

    context.translate(-(camera.x - canvas.width / 2), -(camera.y - canvas.height / 2));
     //   console.log("FRAME_____________")
    frame.forEach(e => {
        if (e.camera != true)
            drawBall(e)
    })
}


const drawBall = (ball) => {

//    console.log(ball)
    context.beginPath()
    context.arc(ball.x, ball.y, ball.radius, 0, Math.PI * 2)
    context.fillStyle = "rgb(0,0,0)"
    context.fill()

}
