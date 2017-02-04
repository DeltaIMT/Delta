package core.clientView

import core.spatial.Zone

abstract class ClientView(id : String) {

  def dataToViewZone(): Zone

  def onNotify(any: Any): Unit

  def onDisconnect(any: Any): Unit

  def fromListToClientMsg(list: List[Any]): String



}
