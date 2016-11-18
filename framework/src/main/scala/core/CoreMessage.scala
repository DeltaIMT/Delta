package core

import akka.actor.ActorRef

object CoreMessage {
  case class DeleteClient(id: String)
  case class AddClient(id: String, playerActorRef: ActorRef)
  case class Command(id : String,command : String )
  case class ChangeActor(id: String, next : ActorRef)
  case class SetProvider(actor : ActorRef)
  case class OnConnect(actor: ActorRef)
  case class PlayersUpdate(json: String)
  case class TransfertTo(id: String, host: ActorRef)
  case class Transfert(id: String, element: Any)
}
