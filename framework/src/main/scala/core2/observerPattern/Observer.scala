package core2.observerPattern

import akka.actor.ActorRef
import core.CoreMessage.Disconnect

class Observer(val id : String ,val client : ActorRef) {

  def onDisconnect() : Unit = client ! Disconnect

}
