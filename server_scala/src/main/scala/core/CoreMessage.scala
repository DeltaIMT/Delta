package core

import akka.actor.ActorRef

/**
  * Created by thoma on 27/10/2016.
  */
object CoreMessage {
  case class DeleteClient(id: String)
  case class AddClient(id: String, playerActorRef: ActorRef)
  case class Command(id : String,command : String )
  case class ChangeActor(id: String, next : ActorRef)
}
