package game.actorPerPlayer

import akka.actor.{Actor, ActorRef, Props}
import core.CoreMessage.{AddClient, ChangeActor}

/**
  * Created by vannasay on 21/10/16.
  */
class ManagerActorPerPlayer() extends Actor{
  override def receive: Receive = {
    case AddClient(id, playerActorRef) => {
      val actor = context.actorOf(Props(new ActorPerPlayer(id, playerActorRef)),"actor" + id)
      sender ! ChangeActor(id, actor)
    }
  }
}
