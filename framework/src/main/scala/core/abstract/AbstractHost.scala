package core.`abstract`

import akka.actor.Actor
import core.CoreMessage.{Tick, _}
import core.HostPool
import core.user_import.{Element, Zone}
import kamon.trace.Tracer

abstract class AbstractHost(val hostPool: HostPool, val zone: Zone) extends Actor {

  var elements = collection.mutable.HashMap[String,Element]()
  var methods = collection.mutable.HashMap[String,Any => Unit]()
  def tick() = {}
  def clientInput(id :String ,data: String) = {}
  def getNum = (zone.x.toInt/zone.w).toInt + "-" + (zone.y.toInt/zone.w).toInt


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
      Tracer.withNewContext("host_tick_"+ getNum , true) {
        tick()
      }
    }

    case ClientInput(id: String, data: String)=> {
      Tracer.withNewContext("host_input_"+ getNum   , true) {
        clientInput(id, data)
      }
    }

    case Method(method, args) => {
      Tracer.withNewContext("host_[" +  method + "]_" + getNum   , true) {
        methods(method)(args)
      }
    }

    case _ => {}
  }
}
