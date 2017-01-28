package rebound

import core.{Host, HostPool}
import core.user_import.Zone
import play.api.libs.json.Json
import stgy.Unity

import scala.util.Random

class RbndHost(hostPool: HostPool[RbndHost], zone: Zone) extends Host(hostPool, zone) {


  override def tick = {

    val balls = elements.values collect { case x: Ball => x }
    var walls = elements.values collect { case x: Wall => x }

    balls.foreach(b => {
      b.x += b.vx
      b.y += b.vy
      b.notifyClientViews
    })

    walls.foreach(w => {
      balls.foreach(b => {
        w.collide(b)
      })

      if (w.shouldDie)
        elements -= w.id
    })


    elements foreach { elem => {
      val e = elem._2
      if (!zone.contains(e)) {
        hostPool.getHyperHost(e.x, e.y).call(host => host.elements += elem._1 -> elem._2)
        elements -= elem._1
      }
    }
    }


  }

  override def clientInput(clientId: String, data: String): Unit = {

    val json = Json.parse(data)
    val x = (json \ "x").get.as[Double]
    val y = (json \ "y").get.as[Double]
    val x2 = (json \ "x2").get.as[Double]
    val y2 = (json \ "y2").get.as[Double]
    var walls = elements.values collect { case x: Wall => x }
    var wall = walls.filter(w => w.clientId == clientId)
    if (wall.nonEmpty) elements -= wall.head.id

    val objId = Random.alphanumeric.take(10).mkString
    val newWall = new Wall(x, y, x2, y2, objId, clientId)
    elements += objId -> newWall
  }


}
