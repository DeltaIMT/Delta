package core.clientView

import akka.actor.ActorRef
import core.Ref

class ClientViewRef[T <: ClientView](val actor : ActorRef) extends Ref[T] {

}
