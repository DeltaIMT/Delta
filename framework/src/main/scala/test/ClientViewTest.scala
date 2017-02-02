package test

import akka.actor.ActorRef
import core2.clientView.ClientView
import core2.spatial.Zone


class ClientViewTest(client :ActorRef)  extends ClientView(client){
  // USER JOB
  override def dataToViewZone(): SquareZone = ???

  override def onNotify(any: Any): Unit = ???

  override def onDisconnect(any: Any): Unit = ???

  override def fromListToClientMsg(list: List[Any]): String = ???
}
