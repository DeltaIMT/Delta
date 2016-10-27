package actorPerPlayer

import akka.actor.{Actor, ActorRef, Props}

/**
  * Created by vannasay on 21/10/16.
  */
class ManagerActorPerPlayer(Provider: ActorRef) extends Actor{
  override def receive: Receive = {
    case AddClient(id, playerActorRef) => {
      val actor = context.actorOf(Props[ManagerActorPerPlayer],"actor" + id)
      Provider ! ChangeActor(id, actor)
    }
  }
}
