package core.host

import akka.actor.Actor
import core.CoreMessage.{Call, ClientInput}


class HostActor[T <: InputReceiver](val host : T) extends Actor {
  override def receive: Receive = {

    //make the host execute the function called by the sender
    case call:Call[T] =>{
        call.func(host)
    }

    //give the data sent by the client with its id to the host that will process it
    case ClientInput(id: String, data: String)=> {
      host.clientInput(id, data)
    }

    case _ => { println("RECEIVE HOST")}
  }
}
