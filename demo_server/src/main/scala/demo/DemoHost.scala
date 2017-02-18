package demo

import core.host.{Host, HostPool, HostRef}
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json
import shapeless.get

import scala.util.Random

class DemoHost(zone: SquareZone, val pos: Int) extends Host(zone){



  override def getViewableFromZone(id : String ,zone: Zone): Iterable[Viewable] = {
    Iterable[Viewable]()
  }

  override def clientInput(id: String, data: String): Unit = {
    val json = Json.parse(data)
    val keydown = (json \ "keydown").toOption
    val keyup = (json \ "keyup").toOption
    val x = (json \ "x").toOption
    val y = (json \ "y").toOption
    val tx = (json \ "tx").toOption
    val ty = (json \ "ty").toOption
  }

  def tick() = {


  }


}
