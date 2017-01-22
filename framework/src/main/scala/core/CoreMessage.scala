package core

import akka.actor.ActorRef
import core.user_import.Element

object CoreMessage {
  case class DeleteClient(id: String)
  case class AddClient(id: String, playerActorRef: ActorRef)
  case class ClientInputWithLocation(id : String, command : String )
  case class ClientInput(id : String, data : String )
  case class ChangeActor(id: String, next : ActorRef)
  case class SetProvider(actor : ActorRef)
  case class ConnectClient(actor: ActorRef)
  case class PlayersUpdate(json: String)
  case class TransfertTo(id: String, host: ActorRef)
  case class Transfert(id: String, element: Element)
  case class Set(id: String, element: Element)
  case class Foreach(f : Element => Unit)
  case class Exec(f : collection.mutable.HashMap[String,Element]=> Unit)
  case class AnyParts(buffer : Int,anys : List[Any])
  case object Tick
  case object Disconnect
  case class Method(method : String, args  : Any)
  case class FromProviderPort(actorRef: ActorRef, port:Int)
  case class ClientDisconnection(port:Int)
  case class Call[T <: Host]( func : T => Unit )
}
