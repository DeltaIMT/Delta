package demo

import core.host.HostPool
import core.observerPattern.Observer
import core.provider.Provider
import core.spatial.Zone
import play.api.libs.json.{JsArray, Json}

import scala.util.Random

class DemoProvider extends Provider[DemoClientView]{

  override def hostsStringToZone(s: String): Option[Zone] = {
    //println(s)
    val json = Json.parse(s).asInstanceOf[JsArray].value
    //println(json)
    val x = json(0).as[Int]
    if(x == -1)
      return None
    val y = json(1).as[Int]
    val w = json(2).as[Int]
    val h = json(3).as[Int]
    Option(new SquareZone(x,y,w,h))
  }

  override def OnConnect(id: String, obs: Observer): Unit = {
    val randx = 200+Random.nextInt(2600)
    val randy = 200+Random.nextInt(2600)
  }

  override def OnDisconnect(id: String, obs: Observer): Unit = {}
}
