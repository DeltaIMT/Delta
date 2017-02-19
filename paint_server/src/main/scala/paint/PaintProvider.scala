package paint


import core.observerPattern.Observer
import core.provider.Provider
import core.spatial.Zone
import play.api.libs.json.{JsArray, Json}

class PaintProvider  extends Provider[PaintClientView]() {

  override def OnConnect(id: String, obs: Observer ) = {

  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
  }

  override def hostsStringToZone(s: String): Option[Zone] = {
      //println(s)
      val json = Json.parse(s).asInstanceOf[JsArray].value
      //println(json)
      val x = json(0).as[Int]
      if(x == -1)
        return None
      val y = json(1).as[Int]
      Option(new SquareZone(x,y,1,1))
    }
}
