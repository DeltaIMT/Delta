package championship


import core.clientView.ClientView
import core.spatial.Zone


class ChpsClientView(id :String) extends ClientView(id) {
  var pos = Vec(1500, 1500)

  override def dataToViewZone(): Zone = new SquareZone(pos.x - 1920/2, pos.y - 1080/2, 1920, 1080)

  override def onNotify(any: Any): Unit = {

    any match {
      case boat: Boat => {
        pos = Vec(boat.x, boat.y)
      }
      case _ => {
        println("notify not matched " )
      }
    }
  }

  override def onDisconnect(any: Any): Unit = {
  }

  override def fromListToClientMsg(list: List[Any]) = {

    val listString = list.map {
          case e: Boat => {
            val targets = e.collectTargets
            val targetString = e.fusionTargetPosition match {
              case Some(v) => if(e.clientId2position.contains(id)) ","+"\"targetX\":"+v.x.toInt+",\"targetY\":"+v.y.toInt else ""
              case None => ""
            }
            val targetsString = targets.map(v => s"""{"x":"${v.x}","y":"${v.y}"}""").mkString("[",",","]")
            s"""{"type":"boat","id":"${e.id}","targets":${if(e.clientId2position.contains(id)) targetsString else "[]"},"health":[${e.health._1},${e.health._2}],"x":"${e.x.toInt}","y":"${e.y.toInt}","size":"${e.size}","ux":"${e.u.x}","uy":"${e.u.y}"${targetString}}"""

          }
          case e: Cannonball => {
            s"""{"type":"cannonball","id":"${e.id}","x":"${e.x.toInt}","y":"${e.y.toInt}"}"""
          }
          case e: Obstacle => {
            s"""{"type":"obstacle","id":"${e.id}","vertices":[${e.shape.vertex.map(v => s"""{"x":"${v.x.toInt}","y":"${v.y.toInt}"}""").mkString(",")}]}"""
          }
          case e: Splash => {
            s"""{"type":"splash","id":"${e.id}","kind":"${e.kind}","x":"${e.x.toInt}","y":"${e.y.toInt}"}"""
          }
    } ++ List(
      s"""{"type":"camera","id":"0","x":"${pos.x.toInt}","y":"${pos.y.toInt}"}""",
      s"""{"type":"other","id":"1","n":"${0}"}""")
    val string = listString.mkString("[", ",", "]")
    Left(string)
  }
}
