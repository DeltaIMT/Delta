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

    case Set(id, element) => {
      elements(id) = element
    }

    case Foreach(f) => {
      elements.foreach(e => f(e._2))

    }


    case _ => {}
  }
}
