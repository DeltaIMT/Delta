package core.`abstract`

import akka.actor.Actor
import core.CoreMessage._
import core.HostPool
import core.user_import.Element

abstract class AbstractHost(val hostPool: HostPool) extends Actor {

  var elements = collection.mutable.HashMap[String,Element]()

  override def receive: Receive = {
    case TransfertTo(element, host) => {
      host ! Transfert(element, elements(element))
      elements -= element
    }

    case Transfert(id, element) => {
      elements += id -> element
    }

    case GetListFilter(f) => {
      sender ! elements.values.filter(f)
    }

    case GetList => {
      sender ! elements
    }

    case Set(id, element) => {
      elements(id) = element
    }

    case _ => {}
  }
}
