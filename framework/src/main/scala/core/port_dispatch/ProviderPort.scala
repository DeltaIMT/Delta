package core.port_dispatch

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AddClient, DeleteClient}
import core.script_filled.UserClientView

/**
  * Created by Cannelle on 22/11/2016.
  */
class ProviderPort extends Actor {
  var map_ID_Port = collection.mutable.HashMap.empty[String,Int]
  var availablePorts = (6001 to 6100).toList

  override def receive: Receive = {
    case AddClient(id: String, client: ActorRef) => {
      client ! availablePorts.head
      println("Add client, new used port : " + availablePorts.head)
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
