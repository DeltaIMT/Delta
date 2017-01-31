package core.`abstract`

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClientView, DeleteClientView}

class HostObserver extends Actor{

  var clientViews = collection.mutable.HashMap[String, ActorRef]()

  override def receive: Receive = {
    case AddClientView(idClient, clientView) => {
      clientViews += idClient -> clientView
    }

    case DeleteClientView(idClient) =>
      clientViews -= idClient
  }
}
