package game.MonoActor

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import core.CoreMessage.{AddClient, ChangeActor}

/**
  * Created by thoma on 27/10/2016.
  */
class ManagerMonoActor(monoactor : ActorRef) extends Actor {
  override def receive: Receive = {
    case AddClient(id : String, client : ActorRef) =>    sender ! ChangeActor(id, monoactor)
  }
}
