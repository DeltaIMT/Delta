package demo

import core.observerPattern.Observer
import core.provider.Provider
import core.spatial.Zone
import play.api.libs.json.{JsArray, Json}

import scala.util.Random

class DemoProvider extends Provider[DemoClientView]{

  frequency = 60

  override def hostsStringToZone(s: String): Option[Zone] = {
    val json = Json.parse(s).asInstanceOf[JsArray].value
    val x = json(0).as[Int]
    if(x == -1)
      return None
    val y = json(1).as[Int]
    val w = json(2).as[Int]
    val h = json(3).as[Int]
    Option(new SquareZone(x,y,w,h))
  }

  override def OnConnect(id: String, obs: Observer): Unit = {
    val position = Vec( Random.nextInt(2000)+500,Random.nextInt(2000)+500)
    val ball = new Ball(id,position)
    ball.radius=20
    ball.sub(obs)
    Demo.HP.getHost(position).call(  _.idMapBalls += id -> ball )
  }

  override def OnDisconnect(id: String, obs: Observer): Unit = {}
}
