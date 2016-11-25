package core.port_dispatch

import akka.actor.FSM.->
import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AddClient, DeleteClient, PlayersUpdate}
import core.script_filled.UserClientView

class ProviderPort extends Actor {
  var map_ID_Port = collection.mutable.HashMap.empty[String,Int]
  var availablePorts = (9001 to 9100).toList

  override def receive: Receive = {
    case AddClient(id: String, client: ActorRef) => {
      client ! PlayersUpdate(""+availablePorts.head)
      println("Add client, new used port : " + availablePorts.head)
      map_ID_Port += id ->availablePorts.head
      availablePorts = availablePorts.tail
      println("Add client, available ports : " + availablePorts.mkString(","))

    }

    case DeleteClient (id: String) => {
      availablePorts = map_ID_Port(id)::availablePorts
      println("Delete client, new available port : " + map_ID_Port(id))
      map_ID_Port -= id
      println("Delete port, available ports : " + availablePorts.mkString(","))
    }
  }
}
