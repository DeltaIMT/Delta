package core.`abstract`

import akka.actor.Actor
import core.CoreMessage.ConnectClient
import core.HostPool
import core.script_filled.UserClientView

class AbstractSpecialHost(val hostPool: HostPool) extends Actor{

  def OnConnect(clientView: UserClientView) = Unit

  override def receive: Receive = {
    case ConnectClient(clientActorRef) => {
      val clientView = new UserClientView(hostPool, clientActorRef)
      OnConnect(clientView)
    }

    case _ => {}
  }
}
