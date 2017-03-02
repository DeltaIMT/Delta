package core.host

import akka.actor.ActorRef
import core.CoreMessage._
import core.Ref

//A class that aggregates the actor and send the messages to it for the user
class HostRef[T <: InputReceiver](val actor: ActorRef) extends Ref[T] {
  //sends to the actor the inputs sent by the client with its id
  def clientInput(id: String, data: String) = actor ! ClientInput(id, data)
}
