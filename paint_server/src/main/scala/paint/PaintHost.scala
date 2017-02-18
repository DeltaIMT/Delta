package paint


import core.host.{Host, HostPool}
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer
import scala.util.Random

class PaintHost(zone: Zone) extends Host( zone) {

  val id2cameraNotifier = collection.mutable.HashMap[String, String]()
  var elements = List[Point]() //TODO try to work with ListBuffer instead ?

  override def clientInput(id :String, data: String) = {

    val json = Json.parse(data)
    val x = (json \ "mousePos" \ "x").get.as[Double]
    val y = (json \ "mousePos" \ "y").get.as[Double]
    val order = (json \ "order").get.as[Int]
    val color = (json \ "c").get.as[Array[Int]]
    val thickness = (json \ "t").get.as[Int]
    val cut = (json \ "cut").get.as[Int]
    val erasing = (json \ "erase").get.as[Int]

    if (erasing == 0) {
      val point = new Point(id, x, y, order, color, thickness, cut)
      elements = point :: elements
    }
    else {
      // if the space covered by the point (x,y,thickness values) is over the center of an existing point and the
      // client asking for the deletion if the same as the one which drew the existing point, then delete it;
      // if the previous element in the line is in this host, set its "cut" attribute to 2 (right cut), else set "cut"
      // to 1 (left cut) in the next element
      elements.foreach(p => if((id == p.id) && (x - thickness < p.x) && (p.x < x + thickness) && (y - thickness < p.y)
        && (p.y < y + thickness)) {
          var next = elements.find(np => np.order == p.order + 1) match {
            case Some(nextP) => nextP.cut = 1
            case None => var previous = elements.find(pp => pp.order == p.order - 1) match {
                case Some(prevP) => prevP.cut = 2
                case None =>
              }
          }
      })
      elements = elements.filterNot(p => (id == p.id) && (x - thickness < p.x) && (p.x < x + thickness)
        && (y - thickness < p.y) && (p.y < y + thickness))
    }

    HostPool[PaintHost,PaintHostObserver].hostObserver.call(ho => ho.associateToClientView(id, x, y))
  }

  def  getViewableFromZone(id: String,zone : Zone) = {
    elements
  }
}
