package game.MonoActor

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive

/**
  * Created by thoma on 27/10/2016.
  */
class ManagerMonoActor(monoactor : ActorRef) extends Actor {
  override def receive: Receive = ???
}
