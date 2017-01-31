package core

import akka.actor.ActorRef
import akka.util.Timeout
import core.CoreMessage.{Call, CallTrace, ClientInput}
import scala.concurrent.duration._


class HyperHostObserver(val hostObserver: ActorRef) {
  implicit val timeout = Timeout(1.second)

  def clientInput(id :String , data: String) = hostObserver ! ClientInput(id, data)

  def call(func : HostObserver=> Unit ) = {
    hostObserver ! Call(func)
  }

  def callTrace(func : HostObserver=> Unit, name: String ) = {
    hostObserver ! CallTrace(func,name)
  }
}
