package demo

import core.clientView.ClientView
import core.host.HostPool
import core.spatial.Zone

class DemoClientView(id : String) extends ClientView(id) {
  var pos = Vec(1500, 1500)

  override def dataToViewZone(): Zone = new SquareZone(pos.x - 1920/2, pos.y - 1080/2, 1920, 1080)

  override def onNotify(any: Any): Unit = {}

  override def onDisconnect(any: Any): Unit = {}

  override def fromListToClientMsg(list: List[Any]) = {
    Left("")
  }
}
