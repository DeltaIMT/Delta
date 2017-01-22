package core.`abstract`

import akka.actor.Actor
import core.CoreMessage._
import core.user_import.Zone
import core.{HostPool, Host}
import kamon.trace.Tracer


class ContainerHost[T <: Host](val hostPool: HostPool[T], val zone: Zone, val insideHost : T) extends Actor {

  insideHost.setContainer(self)
  def getNum = (zone.x.toInt/zone.w).toInt + "-" + (zone.y.toInt/zone.w).toInt

  override def receive: Receive = {

    case call:Call[T] =>{
          call.func(insideHost)
    }


    case ClientInput(id: String, data: String)=> {
      Tracer.withNewContext("host_input_"+ getNum   , true) {
        insideHost.clientInput(id, data)
      }
    }

    case _ => { println("RECEIVE HOST")}
  }
}
