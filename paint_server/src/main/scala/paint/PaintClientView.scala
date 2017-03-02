package paint


import core.clientView.ClientView
import core.spatial.Zone


import scala.collection.mutable.ListBuffer
import scala.util.Random

class PaintClientView (id : String) extends ClientView(id) {

  var x : Double = 0
  var y : Double = 0

  override def onNotify(any: Any): Unit = {
    any match {
      case cameraNotifier : CameraNotifier => {
        x = cameraNotifier.camerax
        y = cameraNotifier.cameray
      }
    }
  }

  override def dataToViewZone(): Zone = new SquareZone(x - 1500, y - 1500, 3000, 3000)

  override def fromListToClientMsg(list: List[Any]) = {
    list match {
      case points: List[Point] => {
        val pointStrings = points.map(p =>
          s"""{"id":"${p.id}","x":${p.x},"y":${p.y},"order":${p.order},"c":[${p.color(0)
          },${p.color(1)},${p.color(2)}],"t":${p.thickness},"cut":${p.cut}}""")
        val string = pointStrings.mkString("[", ",", "]")
        //println("STARTING TO SEND : " + string)
        Left(string)
      }
    }
  }

}
