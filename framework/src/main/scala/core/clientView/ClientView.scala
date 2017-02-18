package core.clientView

import java.nio.ByteBuffer

import core.spatial.Zone

abstract class ClientView(val id : String) {

  def dataToViewZone(): Zone

  def onNotify(any: Any): Unit

  def onDisconnect(any: Any): Unit

  def fromListToClientMsg(list: List[Any]): Either[String,ByteBuffer]



}
