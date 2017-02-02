package core

import akka.actor.ActorRef
import core.host.Host


object CoreMessage {
  case class DeleteClient(id: String)
  case class AddClient(id: String, playerActorRef: ActorRef)
  case class ClientInputWithLocation(id : String, command : String )
  case class ClientInput(id : String, data : String )
  case class ChangeActor(id: String, next : ActorRef)
  case class SetProvider(actor : ActorRef)
  case class ConnectClient(actor: ActorRef)
  case class PlayersUpdate(json: String)
  case class AnyParts(buffer : Int,anys : List[Any])
  case object Tick
  case object Disconnect
  case class Method(method : String, args  : Any)
  case class FromProviderPort(actorRef: ActorRef, port:Int)
  case class ClientDisconnection(port:Int)
  case class Call[T]( func : T => Unit )
  case class CallTrace[T <: Host]( func : T => Unit , name : String)
  case class AddClientView(idClient: String, clientViewRef: ActorRef)
  case class DeleteClientView(idClient: String)
  case object UpdateClient
}
