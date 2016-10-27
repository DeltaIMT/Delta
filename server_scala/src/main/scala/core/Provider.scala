package core

import akka.actor.{Actor, ActorRef}
import core.CoreMessage.{AddClient, ChangeActor, Command, DeleteClient}


object Provider {

  def apply(default : ActorRef) = {
  new Provider(default)
  }

}

class Provider(default : ActorRef) extends Actor {

  var map_ID_Actor = collection.mutable.HashMap.empty[String,ActorRef]
  var map_ID_Client = collection.mutable.HashMap.empty[String,ActorRef]

  override def receive: Receive = {

    case AddClient(id: String, client: ActorRef) => {
      map_ID_Client(id) = client
      map_ID_Actor(id) = default

      default ! AddClient(id,map_ID_Client(id))
    }

    case DeleteClient(id : String) => {
      map_ID_Actor(id) ! DeleteClient(id)
      map_ID_Actor(id) = null
      map_ID_Client(id) = null
    }

    case Command(id:  String, txt : String) =>{
      map_ID_Actor(id) ! Command(id,txt)
    }

    case ChangeActor(id: String, next : ActorRef) =>{
      next ! AddClient(id,map_ID_Client(id) )
      map_ID_Actor(id) = next
    }
  }


}
