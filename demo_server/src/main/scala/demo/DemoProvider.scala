package demo

import core.observerPattern.Observer
import core.provider.Provider
import core.spatial.Zone
import play.api.libs.json.{JsArray, Json}

import scala.util.Random
import jason._

class DemoProvider extends Provider[DemoClientView]{

  frequency = 60

  override def hostsStringToZone(s: String): Option[Zone] = {
    val json = Json.parse(s).asInstanceOf[JsArray].value
    val x = json(0).as[Int]
    val y = json(1).as[Int]
    Option(new SquareZone(x,y,0,0))
  }

  override def OnConnect(id: String, obs: Observer): Unit = {
    val position = Vec( Random.nextInt(900),Random.nextInt(900))
    val ball = new Ball(position,Vec())
    Demo.HP.getHost(position).call(  _.balls ::= ball )
  }

  override def OnDisconnect(id: String, obs: Observer): Unit = {}
}
