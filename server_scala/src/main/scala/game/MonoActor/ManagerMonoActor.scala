package game.monoActor

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClient, ChangeActor}


class ManagerMonoActor(monoactor : ActorRef) extends Actor {
  override def receive: Receive = {
    case AddClient(id : String, client : ActorRef) =>    sender ! ChangeActor(id, monoactor)
  }
}
