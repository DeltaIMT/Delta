package paint


import core.host.{Host, HostPool}
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json

import scala.util.Random

class PaintHost(zone: Zone) extends Host( zone) {

  val id2cameraNotifier = collection.mutable.HashMap[String, String]()
  var elements = List[Point]()

  override def clientInput(id :String, data: String) = {

    val json = Json.parse(data)
    val x = (json \ "mousePos" \ "x").get.as[Double]
    val y = (json \ "mousePos" \ "y").get.as[Double]
    val lineId = (json \ "lineId").get.as[String]
    val order = (json \ "order").get.as[Int]
    val color = (json \ "c").get.as[Array[Int]]
    val thickness = (json \ "t").get.as[Int]
    val point = new Point(x, y, lineId, order, color, thickness)
    elements = point::elements

    HostPool[PaintHost,PaintHostObserver].hostObserver.call(ho => ho.associateToClientView(id, x, y))
  }

  def  getViewableFromZone(id: String,zone : Zone) = {
    elements
  }

}
