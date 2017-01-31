package core.`abstract`

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import core.CoreMessage._
import core.{Host, HostObserver, HostPool}
import kamon.trace.Tracer

class ContainerHostObserver[T <: Host, U <: HostObserver](val hostPool: HostPool[T, U], val insideHostObserver : U) extends Actor{

  insideHostObserver.setContainer(self)

  override def receive: Receive = {

    case call:Call[U] =>{
      call.func(insideHostObserver)
    }

    case call:CallTrace[U] =>{
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
