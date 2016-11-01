package game.actorPerPlayer

import akka.actor.{Actor, ActorRef, Props}
import core.CoreMessage.{AddClient, ChangeActor}
import game.GameEvent.Tick
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
/**
  * Created by vannasay on 21/10/16.
  */
class ManagerActorPerPlayer() extends Actor{
  override def receive: Receive = {
    case AddClient(id, playerActorRef) => {
      val actor = context.actorOf(Props(new ActorPerPlayer(id, playerActorRef)),"actor" + id)
      val cancellable  = context.system.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, actor, Tick())
      sender ! ChangeActor(id, actor)
    }
  }
}
