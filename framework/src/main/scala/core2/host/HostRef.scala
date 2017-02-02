package core2.host

import akka.actor.ActorRef
import core2.CoreMessage._
import core2.Ref

class HostRef[T <: InputReceiver](val actor: ActorRef) extends Ref[T] {
  def clientInput(id: String, data: String) = actor ! ClientInput(id, data)
}
