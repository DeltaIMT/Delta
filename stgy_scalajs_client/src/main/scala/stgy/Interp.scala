package stgy.scalajs

import org.scalajs.dom
import dom.document

object Interp {

  document.addEventListener("keydown", (event: dom.KeyboardEvent) => {
    if (event.keyCode == 73) {
      delay -= 10
      println("delay : " + delay)
    }
    if (event.keyCode == 74) {
      delay += 10
      println("delay : " + delay)
    }
  })

  var id2Unit = collection.mutable.Map[String, UnityFront]()
  var id2Arrow = collection.mutable.Map[String, ArrowFront]()
  var camX = 0.0f
  var camY = 0.0f
  var xp = 0.0f
  var numberOfUnit = 0
  var msLastCompute = 0.0f
  var delay = 120.0f
  var lastFrame: StgyFrame = _
  var msLastFrame = 0.0f

  def addFrame(frame: StgyFrame, ms: Double): Unit = {
    lastFrame = frame
    msLastFrame = ms.toFloat
  }

  def interpFrame(ms: Float): StgyFrame = {
    if (lastFrame == null) return null
    val fromLastCompute = ms - msLastCompute
    val toLastFrame = msLastFrame + delay - msLastCompute
    val zeroToOne = fromLastCompute / toLastFrame
    val lambda0: Float = 1.0f - zeroToOne
    val lambda1: Float = 1.0f - lambda0.toFloat
    //    units: List[UnityFront]
    val arrows =
      lastFrame.arrows.map(a => {
        if (!id2Arrow.contains(a.id)) {
          id2Arrow += a.id -> a
        }
        val computeArrow = id2Arrow(a.id)
        val newX = (computeArrow.x * lambda0 + a.x * lambda1).toInt
        val newY = (computeArrow.y * lambda0 + a.y * lambda1).toInt
        val newArrow = ArrowFront(a.id, newX, newY)
        id2Arrow(a.id) = newArrow
        newArrow
      })
    camX = camX * lambda0 + lastFrame.camX * lambda1
    camY = camY * lambda0 + lastFrame.camY * lambda1
    xp = lastFrame.xp
    numberOfUnit = lastFrame.numberOfUnit
    msLastCompute = ms
    StgyFrame(lastFrame.units, arrows, lastFrame.camX, lastFrame.camY, xp, numberOfUnit)
  }

}


