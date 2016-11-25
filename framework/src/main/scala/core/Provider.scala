package core

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClient, Command, ConnectClient}

class Provider(hosts: HostPool, specialHost: ActorRef) extends Actor{

  var clientRef : ActorRef = null

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      specialHost ! ConnectClient(playerActorRef)
    }

    case x:Command => {  }

    case _ => {}
  }
}
