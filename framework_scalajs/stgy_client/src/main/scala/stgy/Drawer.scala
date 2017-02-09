
package stgy.scalajs


import org.scalajs.dom.html.Image
import stgy.scalajs.WebApp.Context
import sun.net.www.content.image.png

object Drawer {


  def draw(context : Context,frame : StgyFrame)= {
    context.setTransform(1, 0, 0, 1, 0, 0)
    val w = context.canvas.width
    val h = context.canvas.height
    val cameraX = clamp(frame.camX.toInt -w/2, 0 , 3000-w/2 )
    val cameraY = clamp(frame.camY.toInt -h/2, 0 , 3000-h/2 )
    context.translate(-cameraX, -cameraY)

    context.beginPath()
    context.rect(cameraX, cameraY, w , h)
    context.fillStyle = "rgb(200,200,200"
    context.fill()


    frame.units.foreach( u => {
      context.beginPath()
      context.arc(u.x,u.y,20,0,Math.PI*2)
      context.fillStyle = (u.color*u.health)()
      context.fill()
      context.closePath()
    })

    frame.arrows.foreach( a => {
      context.beginPath()
      context.arc(a.x,a.y,3,0,Math.PI*2)
      context.fillStyle = "rgb(0,0,0)"
      context.fill()
      context.closePath()
    })
  }

  def clamp(x : Int, a: Int, b: Int) = Math.max(Math.min(x,b),a)

}
