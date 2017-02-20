
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
    return Math.min(Math.max(value, min), max)
}
const background = new Image()
background.src = './texture/waterTex.png'

const cam = {}
module.exports.cam = () => cam




let splashsId = []


module.exports.draw = (frame, msElapsed, time, ping) => {

    let boats = []
    let cannonballs = []
    let obstacles = []
    let splashs = []
    let other = {}
    let currentPos = {}
    if ((frame) !== undefined) {
        boats = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'boat'
        }).map(k => frame[k])

        cannonballs = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'cannonball'
        }).map(k => frame[k])

        splashs = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'splash' && (!splashsId.includes(frame[k].id))
        }).map(k => frame[k])

        obstacles = Object.keys(frame).filter(k => {
            return typeof (frame[k].type) !== undefined && frame[k].type == 'obstacle'
        }).map(k => frame[k])

        splashs.forEach(s =>
            splashsId.push(s.id)
        )

        if (frame['1'] != undefined)
            other = frame['1']

        if (frame['0'] !== undefined)
            currentPos = frame['0']
    }

    // clamp the camera position to the world bounds while centering the camera around the snake                    
    cam.x = parseInt(clamp(currentPos.x - canvas.width / 2, 0, worldSize.x - canvas.width))
    cam.y = parseInt(clamp(currentPos.y - canvas.height / 2, 0, worldSize.y - canvas.height))
    context.setTransform(1, 0, 0, 1, 0, 0)  // because the transform matrix is cumulative

    context.translate(-cam.x, -cam.y)
    const pattern = context.createPattern(background, 'repeat')
    context.beginPath()
    context.rect(cam.x, cam.y, canvas.width + cam.x, canvas.height + cam.y)
    context.fillStyle = pattern
    context.fill()

    drawObstacles(obstacles)
    drawboats(boats, canvasCacheboat)
    drawcannonballs(cannonballs, canvascannonballCache)
    createSplashs(splashs)

    context.translate(cam.x, cam.y)

    context.shadowBlur = 30
    context.shadowColor = "black"
    context.beginPath()
    context.fillStyle = "rgba(" + 150 + ", " + 150 + ", " + 150 + "," + 0.4 + ")"
    context.rect(0, 0, 350, 130)
    context.fill()
    context.shadowBlur = 0
    context.font = "bold 18px Courier New"
    context.fillStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
    if (other !== null){
        context.fillText("Follow the green arrow and" , 10, 30)
        context.fillText("fuse with your partner !",10,50)
    }
    context.fillText("Ping : " + parseInt(ping * 10) / 10.0, 10, 80)
    context.fillText("Draw fps        : " + parseInt(10000.0 / msElapsed) / 10.0, 10, 105)

}


function roundRect(context, x, y, width, height, radius) {
    if (typeof radius === 'number') {
        radius = { tl: radius, tr: radius, br: radius, bl: radius }
    } else {
        let defaultRadius = { tl: 0, tr: 0, br: 0, bl: 0 }
        for (let side in defaultRadius) {
            radius[side] = radius[side] || defaultRadius[side]
        }
    }
    context.beginPath()
    context.moveTo(x + radius.tl, y)
    context.lineTo(x + width - radius.tr, y)
    context.quadraticCurveTo(x + width, y, x + width, y + radius.tr)
    context.lineTo(x + width, y + height - radius.br)
    context.quadraticCurveTo(x + width, y + height, x + width - radius.br, y + height)
    context.lineTo(x + radius.bl, y + height)
    context.quadraticCurveTo(x, y + height, x, y + height - radius.bl)
    context.lineTo(x, y + radius.tl)
    context.quadraticCurveTo(x, y, x + radius.tl, y)
    context.closePath()

}


const createCacheboat = () => {
    const canvasCacheboat = document.createElement('canvas')
    canvasCacheboat.setAttribute('width', 80)
    canvasCacheboat.setAttribute('height', 80)
    const contextboat = canvasCacheboat.getContext('2d')
    contextboat.beginPath()
    contextboat.arc(40, 40, 20, 0, Math.PI * 2)
    contextboat.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + ",0.5)"
    contextboat.shadowBlur = 30
    contextboat.shadowColor = "black"
    contextboat.fill()
    contextboat.shadowBlur = 20
    contextboat.fill()
    contextboat.shadowBlur = 5
    contextboat.fill()
    contextboat.shadowBlur = 3
    contextboat.fill()
    contextboat.shadowBlur = 2
    contextboat.fill()
    return canvasCacheboat
}


const createCachecannonball = () => {
    const canvascannonballCache = document.createElement('canvas')
    canvascannonballCache.setAttribute('width', 40)
    canvascannonballCache.setAttribute('height', 40)
    const contextcannonball = canvascannonballCache.getContext('2d')
    contextcannonball.beginPath()
    contextcannonball.arc(20, 20, 3, 0, Math.PI * 2)
    contextcannonball.fillStyle = "black"
    contextcannonball.fill()
    return canvascannonballCache
}


let timestamp = 0




const drawboats = (boats, canvasCacheboat) => {
    timestamp += 1


    for (const boat of boats) {
        //   context.drawImage(canvasCacheboat, boat.x - 40, boat.y - 40)


        const m = { x: parseFloat(boat.x), y: parseFloat(boat.y) }
        const u = { x: parseFloat(boat.ux), y: parseFloat(boat.uy) }
        const v = { x: -u.y, y: u.x }
        if (boat.targetX !== undefined && boat.targetY !== undefined) {
            let target = { x: parseFloat(boat.targetX), y: parseFloat(boat.targetY) }
            let targetToBoat = Math.sqrt(parseFloat((target.x - m.x) * (target.x - m.x) + (target.y - m.y) * (target.y - m.y)))
            let unitTargetDirection = { x: ((target.x - m.x) / targetToBoat), y: ((target.y - m.y) / targetToBoat) }
            let n = { x: -unitTargetDirection.y, y: unitTargetDirection.x }
            let c = { x: cam.x + canvas.width * 14 / 15, y: cam.y + parseFloat(canvas.height) * 1 / 10 }//arrowCenter
            context.beginPath()
            context.fillStyle = "rgb(20, 200, 50)"//gold
            context.moveTo(c.x + n.x * 5*5, c.y + n.y * 5*5)
            context.lineTo(c.x + n.x * 10*5, c.y + n.y * 10*5)
            context.lineTo(c.x + 10 * unitTargetDirection.x*5, c.y + 10 * unitTargetDirection.y*5)
            context.lineTo(c.x - n.x * 10*5, c.y - n.y * 10*5)
            context.lineTo(c.x - n.x * 5*5, c.y - n.y * 5*5)
            context.lineTo(c.x - 15 * unitTargetDirection.x *5- n.x * 5*5, c.y - 15 * unitTargetDirection.y*5 - n.y * 5*5)
            context.lineTo(c.x - 15 * unitTargetDirection.x*5 + n.x * 5*5, c.y - 15 * unitTargetDirection.y*5 + n.y * 5*5)
            context.lineTo(c.x + n.x * 5*5, c.y + n.y * 5*5)
            context.strokeStyle = "rgb(255,255, 255)"
            context.stroke()
            context.fill()
            context.closePath()
        }
        const length = 100.0 * Math.sqrt(parseFloat(boat.size))
        const width = length / 3.0

        // console.log(m,u,v,length,width )

        context.beginPath()
        context.moveTo(m.x + (-v.x * width / 2.0 + u.x * length / 2.0), m.y + (-v.y * width / 2.0 + u.y * length / 2.0))
        context.lineTo(m.x + (0 + u.x * length), m.y + (0 + u.y * length))
        context.lineTo(m.x + (v.x * width / 2.0 + u.x * length / 2.0), m.y + (v.y * width / 2.0 + u.y * length / 2.0))
        context.lineTo(m.x + (v.x * width / 2.0 - u.x * length / 2.0), m.y + (v.y * width / 2.0 - u.y * length / 2.0))
        context.lineTo(m.x + (v.x * width / 4.0 - u.x * length * 0.6), m.y + (v.y * width / 4.0 - u.y * length * 0.60))
        context.lineTo(m.x + (-v.x * width / 4.0 - u.x * length * 0.6), m.y + (-v.y * width / 4.0 - u.y * length * 0.60))
        context.lineTo(m.x + (-v.x * width / 2.0 - u.x * length / 2.0), m.y + (-v.y * width / 2.0 - u.y * length / 2.0))
        context.closePath()
        context.fillStyle = "rgb( 131,73,44)"
        context.fill()





        drawSail(1.5, length / 4, length / 10, 0.5, 0, m, u, v, width)
        drawSail(1.0, length / 5, length / 10, 0.5, -length / 3, m, u, v, width)
        drawSail(1.2, length / 4, length / 10, 0.5, length / 4, m, u, v, width)

        const flagSizeFactor = 2
        for (const flag of boat.targets) {
            const x = parseInt(flag.x)
            const y = parseInt(flag.y)
            context.beginPath()
            context.moveTo(x, y)
            context.lineTo(x, y - 20 * flagSizeFactor)
            context.lineTo(x + 9 * flagSizeFactor, y - 16 * flagSizeFactor)
            context.lineTo(x, y - 12 * flagSizeFactor)
            context.closePath()
            context.lineWidth = 3
            context.strokeStyle = "rgb(" + 0 + ", " + 0 + ", " + 0 + ")"
            context.stroke()
            context.fillStyle = "rgb(255,0,0)"
            context.fill()

        }

        let healthLeft = (boat.health[0] / (2 + boat.size * 0.50))
        let healthRight = (boat.health[1] / (2 + boat.size * 0.50))

        /*//healthBar
        context.beginPath()
        context.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + "," + 1 + ")"
        context.rect(m.x - 25 - 30, m.y - 100, 50, 10)
        context.fill()
        context.closePath()
        context.beginPath()
        context.fillStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + "," + 1 + ")"
        context.rect(m.x - 25 - 30, m.y - 100, 50.0 * healthLeft, 10)
        context.fill()
        context.closePath()

*/

        let rand = Math.random() * length / 5

        if (healthRight < 0.9) {
            addParticles(m, u, v, length / 3 + rand, width / 2)
        }
        if (healthRight < 0.5) {
            addParticles(m, u, v, -length / 3 + rand, width / 2)
        }
/*
        context.beginPath()
        context.fillStyle = "rgba(" + 0 + ", " + 0 + ", " + 0 + "," + 1 + ")"
        context.rect(m.x - 25 + 30, m.y - 100, 50, 10)
        context.fill()
        context.closePath()
        context.beginPath()
        context.fillStyle = "rgba(" + 0 + ", " + 255 + ", " + 0 + "," + 1 + ")"
        context.rect(m.x - 25 + 30, m.y - 100, 50.0 * healthRight, 10)
        context.fill()
        context.closePath()
*/
        if (healthLeft < 0.9) {

            addParticles(m, u, v, length / 3 - rand, -width / 2)
        }
        if (healthLeft < 0.5) {
            addParticles(m, u, v, -length / 3 - rand, -width / 2)
        }
    }
    updateParticles()
    drawFlammes()
    drawSplashs()

}
let fireParticles = []
let smokeParticles = []
let particles = []

const addParticles = (m, u, v, offsetU, offsetV) => {

    function createFireParticle() {
        let p = {
            lifeSpan: 10,
            alpha: 0.1 + Math.random() * 0.05,
            alphaDecay: 0.2 + Math.random() * 0.3,
            colour: getColourFire(),
            x: m.x + offsetU * u.x + offsetV * v.x,
            y: m.y + offsetU * u.y + offsetV * v.y,
            radius: 3 + Math.random() * 9,
            radiusDecay: Math.random() * 5,
            direction: Math.random() * Math.PI * 2,
            speed: 50 + Math.random() * 150
        }

        fireParticles.push(p)
    }

    function createSmokeParticle() {
        let p = {
            lifeSpan: 10,
            alpha: 0.1 + Math.random() * 0.05,
            alphaDecay: 0.2 + Math.random() * 0.3,
            colour: getColourSmoke(),
            x: m.x + offsetU * u.x + offsetV * v.x,
            y: m.y + offsetU * u.y + offsetV * v.y,
            radius: 6 + Math.random() * 9,
            radiusDecay: Math.random() * 5,
            direction: Math.random() * Math.PI * 2,
            speed: 50 + Math.random() * 50
        }

        smokeParticles.push(p)
    }

    function getColourFire() {
        let red = parseInt(Math.random() * 125.0 + 130.0)
        let green = parseInt(Math.random() * 85.0)
        return "rgba(" + red + ", " + green + ", 0,0.8)"
    }
    function getColourSmoke() {
        let rand = Math.random() * 35.0
        let rands = parseInt(rand)
        return "rgba(" + rands + "," + rands + ", " + rands + ", 0.10" + ")"
    }

    if (fireParticles.length < 10000) {
        createFireParticle()
    }

    if (smokeParticles.length < 5000) {
        createSmokeParticle()
    }

}


const updateParticles = () => {
    let p
    for (var i = fireParticles.length - 1; i >= 0; i--) {
        p = fireParticles[i]
        p.lifeSpan -= 1
        if (p.lifeSpan <= 0) {
            fireParticles.splice(i, 1)
        }
        p.alpha -= p.alphaDecay * 1 / 1000
        if (p.alpha <= 0) {
            fireParticles.splice(i, 1)
        }
        p.radius -= p.radiusDecay * 1 / 1000
        if (p.radius <= 0) {
            fireParticles.splice(i, 1)
        }
        p.x += p.speed * Math.cos(p.direction) * 1 / 1000
        p.y += p.speed * Math.sin(p.direction) * 1 / 1000
    }

    for (var i = smokeParticles.length - 1; i >= 0; i--) {
        p = smokeParticles[i]

        p.lifeSpan -= 1
        if (0 >= p.lifeSpan) {
            smokeParticles.splice(i, 1)
        }

        p.alpha -= p.alphaDecay * 1 / 1000
        if (p.alpha <= 0) {
            smokeParticles.splice(i, 1)
        }

        p.radius -= p.radiusDecay * 1 / 1000
        if (p.radius <= 0) {
            smokeParticles.splice(i, 1)
        }

        p.x += p.speed * Math.cos(p.direction) * 1 / 1000
        p.y += p.speed * Math.sin(p.direction) * 1 / 1000
    }

}

const drawFlammes = () => {

    fireParticles.forEach(p => {
        context.fillStyle = p.colour
        context.beginPath()
        context.arc(p.x, p.y, p.radius, 0, 2 * Math.PI)
        context.fill()
        context.closePath()
    })

    smokeParticles.forEach(p => {
        context.fillStyle = p.colour
        context.beginPath()
        context.arc(p.x, p.y, p.radius, 0, 2 * Math.PI)
        context.fill()
        context.closePath()
    })
}



const drawSail = (span, frontCurve, backCurve, curveFactor, offsetPosition, m, u, v, width) => {
    context.beginPath()
    context.moveTo(offsetPosition * u.x + m.x + v.x * span * width, offsetPosition * u.y + m.y + v.y * span * width)
    context.bezierCurveTo(offsetPosition * u.x + m.x + v.x * span * width * curveFactor + u.x * backCurve, offsetPosition * u.y + m.y + v.y * span * width * curveFactor + u.y * backCurve,
        offsetPosition * u.x + m.x - v.x * span * width * curveFactor + u.x * backCurve, offsetPosition * u.y + m.y - v.y * span * width * curveFactor + u.y * backCurve,
        offsetPosition * u.x + m.x - v.x * span * width, offsetPosition * u.y + m.y - v.y * span * width)
    context.bezierCurveTo(offsetPosition * u.x + m.x - v.x * span * width * curveFactor + u.x * frontCurve, offsetPosition * u.y + m.y - v.y * span * width * curveFactor + u.y * frontCurve,
        offsetPosition * u.x + m.x + v.x * span * width * curveFactor + u.x * frontCurve, offsetPosition * u.y + m.y + v.y * span * width * curveFactor + u.y * frontCurve,
        offsetPosition * u.x + m.x + v.x * span * width, offsetPosition * u.y + m.y + v.y * span * width)
    context.fillStyle = "rgb(255,255,255)"
    context.fill()
}

const drawcannonballs = (cannonballs, canvascannonballCache) => {
    for (const cannonball of cannonballs) {
        context.drawImage(canvascannonballCache, cannonball.x - 20, cannonball.y - 20)
    }
}
const createSplashs = (splashs) => {
    const num = 20
    splashs.forEach(splash => {
        function createParticle(lifeSpan, color, r, speed) {
            let vx = Math.random() * 1.4 - 0.7
            let vy = Math.random() * 1.4 - 0.7
            let len = Math.sqrt(vx * vx + vy * vy) * (Math.random() * 0.3 + 0.7)
            vx /= len
            vy /= len
            vx *= speed
            vy *= speed
            let p = {
                lifeSpan: lifeSpan,
                colour: color,
                x: parseInt(splash.x),
                y: parseInt(splash.y),
                radius: r,
                vx: vx,
                vy: vy

            }
            return p
        }
        if (splash.kind == "water") {
            let color1 = { r: 220, g: 255, b: 255, a: 1.0 }
            let color2 = { r: 20, g: 150, b: 200, a: 0.8 }
            for (let i = 0; i < num; i++)
                particles.push(createParticle(20, color1, Math.random() * 3 + 2, 1), createParticle(20, color2, Math.random() * 5 + 2, 1))
        }
        else if (splash.kind == "boat") {
            //FIRE
            for (let i = 0; i < num; i++) {
                let red = parseInt(Math.random() * 255)
                let green = parseInt(red * Math.random())
                let blue = parseInt(green / 2)
                let color = { r: red, g: green, b: blue, a: 1.0 }
                particles.push(createParticle(20, color, Math.random() * 3 + 2, 2))
            }
            //SMOKE
            for (let i = 0; i < num; i++) {
                let red = parseInt(Math.random() * 100)
                let green = parseInt(red * Math.random())
                let blue = parseInt(green / 2)
                let color = { r: red, g: green, b: blue, a: 1.0 }
                particles.push(createParticle(40, color, Math.random() * 6 + 4, 0.4))
            }
        }
        else if (splash.kind == "obstacle") {
            //FIRE
            for (let i = 0; i < num; i++) {
                let red = parseInt(Math.random() * 255)
                let green = parseInt(red * Math.random())
                let blue = parseInt(green / 2)
                let color = { r: red, g: green, b: blue, a: 1.0 }
                particles.push(createParticle(20, color, Math.random() * 3 + 2, 2))
            }
            //SMOKE
            for (let i = 0; i < num; i++) {
                let red = parseInt(Math.random() * 255)
                let green = red
                let blue = red
                let color = { r: red, g: green, b: blue, a: 1.0 }
                particles.push(createParticle(40, color, Math.random() * 6 + 4, 0.4))
            }
        }

    })
}

const drawSplashs = () => {
    let p
    for (var i = particles.length - 1; i >= 0; i--) {
        p = particles[i]
        if (p.lifeSpan <= 0) {
            particles.splice(i, 1)
        } else {
            p.lifeSpan -= 1
            p.x += p.vx
            p.y += p.vy
            context.fillStyle = "rgba(" + p.colour.r + "," + p.colour.g + "," + p.colour.b + "," + p.colour.a * p.lifeSpan / 20.0 + ")"
            context.beginPath()
            context.arc(parseFloat(p.x), parseFloat(p.y), p.radius * Math.sqrt(p.lifeSpan / 20.0), 0, 2 * Math.PI)
            context.fill()
            context.closePath()
        }
    }
}


const drawObstacles = (obstacles) => {
    for (const obstacle of obstacles) {
        context.beginPath()
        context.moveTo(obstacle.vertices[0].x, obstacle.vertices[0].y)
        for (const vertex of obstacle.vertices.slice(1))
            context.lineTo(vertex.x, vertex.y)
        context.fillStyle = "rgb(150,135,120)"
        context.fill()
        context.closePath()
    }
}

const canvasCacheboat = createCacheboat()
const canvascannonballCache = createCachecannonball()