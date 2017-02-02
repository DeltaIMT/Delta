package core2.clientView

import akka.actor.ActorRef
import core2.Ref

class ClientViewRef[T <: ClientView](val actor : ActorRef) extends Ref[T](actor) {

}
