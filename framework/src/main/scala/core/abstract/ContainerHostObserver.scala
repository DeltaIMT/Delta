package core.`abstract`

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import core.CoreMessage._
import core.{Host, HostObserver, HostPool}
import kamon.trace.Tracer

class ContainerHostObserver[T <: Host](val hostPool: HostPool[T], val insideHostObserver : HostObserver) extends Actor{

  insideHostObserver.setContainer(self)

  override def receive: Receive = {

    case call:Call[HostObserver] =>{
      call.func(insideHostObserver)
    }

    case call:CallTrace[HostObserver] =>{
      Tracer.withNewContext("host_observer_method_" +call.name  , true) {
        call.func(insideHostObserver)
      }
    }


    case ClientInput(id: String, data: String)=> {
      Tracer.withNewContext("host_observer_input", true) {
        insideHostObserver.clientInput(id, data)
      }
    }

    case _ => { println("RECEIVE HOST")}
  }
}
