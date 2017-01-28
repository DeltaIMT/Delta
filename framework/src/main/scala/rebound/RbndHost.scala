package rebound

import core.{Host, HostPool, HyperHost}
import core.user_import.Zone
import play.api.libs.json.Json
import scala.util.Random

class RbndHost(hostPool: HostPool[RbndHost], zone: Zone) extends Host(hostPool, zone) {

  val otherWalls = collection.mutable.HashMap[String,List[Wall]]()
  var neighbours = List[HyperHost[RbndHost]]()

  def getNeighbours = {
    if (neighbours.isEmpty) {
      if (zone.x < hostPool.wn * hostPool.w) {
        neighbours ::= hostPool.getHyperHost(zone.x + zone.w, zone.y)
        if (zone.y < hostPool.hn * hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x + zone.w, zone.y + zone.h)
        if (zone.y >= hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x + zone.w, zone.y - zone.h)
      }
      if (zone.x >= hostPool.w) {
        neighbours ::= hostPool.getHyperHost(zone.x - zone.w, zone.y)
        if (zone.y < hostPool.hn * hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x - zone.w, zone.y + zone.h)
        if (zone.y >= hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x - zone.w, zone.y - zone.h)
      }
      if (zone.y < hostPool.hn * hostPool.h)
        neighbours ::= hostPool.getHyperHost(zone.x, zone.y + zone.h)
      if (zone.y >= hostPool.h)
        neighbours ::= hostPool.getHyperHost(zone.x, zone.y - zone.h)
    }
  }

  override def tick = {
    getNeighbours



    val balls = elements.values collect { case x: Ball => x }
    var walls = elements.values collect { case x: Wall => x }


    var otherWallsList = List[Wall]()
    otherWalls.foreach( lw => {
      otherWallsList = otherWallsList++ lw._2
    })


    neighbours.foreach( n => n.call( h => {
      if(!h.otherWalls.contains(zone.toString)){
        h.otherWalls += zone.toString -> Nil
      }
      h.otherWalls(zone.toString) = walls.toList
    }))

    //Faire avancer les boules
    balls.foreach(b => {
      balls.foreach(b2 => {
        if(b != b2){
          b.collide(b2)
        }
      })
    })

    balls.foreach(b => {
      b.x += b.vx
      b.y += b.vy

      if(b.x<20 || b.x > 3000-20 )
        b.vx = -b.vx
      if(b.y<20 || b.y > 3000-20)
        b.vy = -b.vy

      b.notifyClientViews
    })

    otherWallsList.foreach(w => {
      balls.foreach(b => {
        w.collide(b)
      })
      if (w.shouldDie)
      hostPool.getHyperHost(w.x,w.y).call( h => h.elements -= w.id)
    })

    walls.foreach(w => {

      balls.foreach(b => {
        w.collide(b)
      })
      w.frameleft-=1
      if (w.shouldDie || w.frameleft<0)
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


    val objId = Random.alphanumeric.take(10).mkString
    val newWall = new Wall(x, y, x2, y2, objId, clientId)
    elements += objId -> newWall
  }


}
