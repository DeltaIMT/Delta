package game.actorPerPlayer

import akka.actor
import akka.actor.{Actor, ActorRef, Props}
import core.CoreMessage.{AddClient, ChangeActor}
import game.GameEvent._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
/**
  * Created by vannasay on 21/10/16.
  */
class ManagerActorPerPlayer() extends Actor{

  var players = collection.mutable.LinkedHashMap.empty[String, ActorRef]
  players += "manager" -> self

  override def receive: Receive = {
    case AddClient(id, playerActorRef) => {
      val host = context.actorOf(Props(new ActorPerPlayer(id, playerActorRef)),"actor" + id)
      val cancellable  = context.system.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, host, Tick())

      sender ! ChangeActor(id, host)
      host ! ListPlayers(players.clone())
      players += id -> host
    }

    case AskJson => { sender ! PlayerJson("{}")}

    case DeletePlayer(id) => {
      players -= id
    }
  }
}
