package game.spatialHost

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AddClient, ChangeActor}
import game.GameEvent.{AddPlayerData, PlayerData, Vector}

class ManagerSpatialHost (spatialHosts : List[ActorRef]) extends Actor {
  val rand = scala.util.Random
  override def receive: Receive = {
    case AddClient(id : String, client : ActorRef) =>  {

      val playerData = PlayerData(id, Vector(50+0*rand.nextInt(500),50+0*rand.nextInt(1000)) :: List.empty[Vector], 50, rand.nextDouble(), 10, 10, Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), null)
      sender ! ChangeActor(id, spatialHosts(0))
      spatialHosts(0) ! AddPlayerData(playerData)

    }
  }
}

