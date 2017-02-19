package championship





import core.host.HostPool
import core.observerPattern.Observer
import core.provider.Provider
import core.spatial.Zone
import play.api.libs.json.{JsArray, Json}

import scala.util.Random

class ChpsProvider extends Provider[ChpsClientView]{
  val HP = HostPool[ChpsHost, ChpsHostObserver]
  override def OnConnect(id: String, obs: Observer) = {

    val randx = 200+Random.nextInt(2600)
    val randy = 200+Random.nextInt(2600)

    val objId= Random.alphanumeric.take(10).mkString
    val boat  = new Boat(randx,randy,objId)
    boat.sub(obs)
    boat.addClient(id)

    HP.getHost(boat).call( host => {
      host.elements += objId -> boat
      host.clientId2Boat += id -> objId
    })


    HP.hostObserver.call(hostObs => {
      hostObs.notPairedYet += objId -> 1
      hostObs.boats += objId -> boat
    })

  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
  }

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


}
