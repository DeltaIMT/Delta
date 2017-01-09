const initialTime = Date.now()
const getTime = () => { return Date.now() - initialTime }
const timeBetweenFrame = 33.33

var noInterp  = false
document.addEventListener('keydown', function(event) {
    if(event.keyCode == 73) {
        noInterp = !noInterp
        console.log("noInterp : "  + noInterp)
    }
});

class FrameInterp {
    constructor() {
        this.frame0 = {}
        this.frame1 = {}
        this.frame2 = {}
        this.time = 0
        this.keyToInterp = ['x', 'y', 'health' , 'spawning', 'possessing']
    }

    addFrame(frame) {
        this.time = getTime()
    //   console.log("Adding frame at " + this.time)
        this.frame0 = this.frame1
        this.frame1 = this.frame2
        this.frame2 = frame
    }

    getInterp() {

        const t = getTime()
        let lambda = (t - this.time) / timeBetweenFrame
        //   console.log("   Getting interpolation at " + t)
        //   console.log("   Last added frame at      " + this.time)
     //   console.log("   Interpolation at " + lambda * 100 + " %")

        let a = this.frame0
        let b = this.frame1

        if (lambda > 2) {
            return this.frame2
        }
        else if (lambda > 1) {
            a = this.frame1
            b = this.frame2
            lambda = lambda - 1
        }

       if(noInterp)
        {
            a = this.frame1
            b = this.frame2
            lambda=1
        }

        let ax = 1 - lambda
        let bx = lambda
        
 
        //Now we interpolate between a and b with ax and bx proportion

        const keys = Object.keys(b)
        let interp = {}
        // Object.assign(interp, b)
        keys.forEach(k => {
            if ("undefined" !== typeof (a[k])) {
                interp[k] = {}
                const keysInObject = Object.keys(b[k])
                keysInObject.forEach(k2 => {
                    if (this.keyToInterp.includes(k2)) {
                        interp[k][k2] = a[k][k2] * ax + b[k][k2] * bx
    //                    console.log("       interp for " + k2)
                    }
                    else {
                        interp[k][k2] = b[k][k2]
    //                    console.log("       no interp for " + k2)
                    }
                })

            }
            else {
                const keysInObject = Object.keys(b[k])
                interp[k] = {}
                keysInObject.forEach(k2 => {
                    interp[k][k2] = b[k][k2]
    //                console.log("       no past for " + k +" " + k2)
                })
            }
        })

        return interp
    }

}

//const frameInterp = new FrameInterp
module.exports= new FrameInterp
// let t = 0

// const frameLoader = () => {
//     if (t < 10)
//         setTimeout(frameLoader, 100)
//     t += 1
//     frameInterp.addFrame({ id1: { x: t, z: Math.random() } })
// }
// const frameLooker = () => {
//     if (t < 12)
//         setTimeout(frameLooker, 10)
//     let interp = frameInterp.getInterp()
//     console.log("           " +JSON.stringify(interp))
// }

// frameLoader()
// frameLooker()



