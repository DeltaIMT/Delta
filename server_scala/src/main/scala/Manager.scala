import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive

/**
  * Created by vannasay on 27/10/16.
  */
class Manager(Provider: ActorRef) extends Actor{
  override def receive: Receive = {
    case AddClient(id, playerActorRef) => {
      val actor = context.actorOf(Props[ManagerActorPerPlayer],"actor")
      Provider ! ChangeActor(id, actor)
    }
  }
}
