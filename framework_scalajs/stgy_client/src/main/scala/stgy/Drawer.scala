package stgy.scalajs


import stgy.scalajs.WebApp.{Color, Context}

object Drawer {


  def drawCircle(context : Context) = {
    context.beginPath()
    context.fillStyle =Color(50,150,0)()
    context.fillRect(0,0,50,50)
  }

}
