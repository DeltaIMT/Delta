package core.clientView

import java.nio.ByteBuffer

import core.spatial.Zone

abstract class ClientView(val id : String) {

  // define the area that the client must see
  def dataToViewZone(): Zone

  // define the action which should occured when the ClientView received a notify from a Host
  def onNotify(any: Any): Unit

  // convert all the data that should be sent to the client from a list into a message
  def fromListToClientMsg(list: List[Any]): Either[String,ByteBuffer]



}
