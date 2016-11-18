package core

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClient, OnConnect}
import core.script_filled.UserClientView

class Provider(hosts: HostPool, specialHost: ActorRef) extends Actor{

  var clientRef : ActorRef = ???

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      context.system.actorOf(Props[UserClientView])
      specialHost ! OnConnect(playerActorRef)
    }

    case _ => {}
  }
}
