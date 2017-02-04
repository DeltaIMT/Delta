package core.host

import akka.actor.Actor
import core.CoreMessage.{Call, ClientInput}


class HostActor[T <: InputReceiver](val host : T) extends Actor {
  override def receive: Receive = {

    case call:Call[T] =>{
        call.func(host)
    }

    case ClientInput(id: String, data: String)=> {
      host.clientInput(id, data)
    }

    case _ => { println("RECEIVE HOST")}
  }
}