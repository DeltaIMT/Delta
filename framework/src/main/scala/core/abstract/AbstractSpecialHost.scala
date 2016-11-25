package core.`abstract`

import akka.actor.{Actor, ActorRef, Props}
import core.CoreMessage.{ConnectClient, PlayersUpdate}
import core.HostPool
import core.script_filled.UserClientView

class AbstractSpecialHost(val hostPool: HostPool) extends Actor{

  var clients = collection.mutable.HashMap[Int,ActorRef]()
  var idClient = 0

  def OnConnect(clientId: Int) : Unit = {}

  override def receive: Receive = {
    case ConnectClient(clientActorRef) => {
      val clientView = context.actorOf(Props(new UserClientView(hostPool, clientActorRef)))
      clients += (idClient -> clientView)
      OnConnect(idClient)
      clientActorRef ! PlayersUpdate("you are connected")
      idClient += 1
    }

    case _ => {}
  }
}
