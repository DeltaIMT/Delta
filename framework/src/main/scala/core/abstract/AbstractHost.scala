package core.`abstract`

import akka.actor.Actor
import core.CoreMessage.{Transfert, TransfertTo}
import core.HostPool

abstract class AbstractHost(val hostPool: HostPool) extends Actor {

  var elements = collection.mutable.HashMap[String,Any]()

  override def receive: Receive = {
    case TransfertTo(element, host) => {
      host ! Transfert(element, elements(element))
      elements -= element
    }

    case Transfert(id, element) => {
      elements += id -> element
    }
    case _ => {}
  }
}
