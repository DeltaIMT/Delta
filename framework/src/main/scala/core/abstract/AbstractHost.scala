package core.`abstract`

import akka.actor.Actor
import core.CoreMessage.{Tick, _}
import core.HostPool
import core.user_import.Element

abstract class AbstractHost(val hostPool: HostPool) extends Actor {

  var elements = collection.mutable.HashMap[String,Element]()
  var methods = collection.mutable.HashMap[String,Any => Unit]()
  def tick() = {}
  def clientInput(id :String ,data: String) = {}

  override def receive: Receive = {

    case Transfert(id, element) => {
      elements += id -> element
    }

    case Set(id, element) => {
      elements(id) = element
    }

    case Foreach(f) => {
      elements.foreach(e => f(e._2))

    }

    case Exec(f) => {
      f(elements)
    }

    case Tick =>{
      tick()
    }

    case ClientInput(id: String, data: String)=> {
      clientInput(id,data)
    }

    case Method(method, args) => {
      methods(method)(args)
    }

    case _ => {}
  }
}
