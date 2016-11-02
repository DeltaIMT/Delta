package game.spatialHost

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AddClient, ChangeActor}

class ManagerSpatialHost (spatialHosts : List[ActorRef]) extends Actor {
  override def receive: Receive = {
    case AddClient(id : String, client : ActorRef) =>    sender ! ChangeActor(id, monoactor)
  }
}

