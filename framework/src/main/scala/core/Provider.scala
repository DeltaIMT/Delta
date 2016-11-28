package core

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClient, Command, ConnectClient, DeleteClient}

class Provider(hosts: HostPool, specialHost: ActorRef) extends Actor{

  var clientRef : ActorRef = null

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      specialHost ! AddClient(id, playerActorRef)
    }

    case DeleteClient(id)=> {
      specialHost ! DeleteClient(id)
    }

    case x:Command => {  }

    case _ => {}
  }
}
