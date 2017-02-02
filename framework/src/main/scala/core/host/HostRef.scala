package core.host

import akka.actor.ActorRef
import core.CoreMessage._
import core.Ref

class HostRef[T <: InputReceiver](val actor: ActorRef) extends Ref[T] {
  def clientInput(id: String, data: String) = actor ! ClientInput(id, data)
}
