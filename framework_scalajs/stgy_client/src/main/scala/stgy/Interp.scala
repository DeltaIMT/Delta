package stgy.scalajs


object Interp {

  var frame0 : StgyFrame = _
  var frame1 : StgyFrame = _

  def addFrame(frame : StgyFrame): Unit = {
      frame0 = frame1
      frame1 = frame
  }

  def interpFrame(ms : Double) : StgyFrame = {
    frame1
  }

}
