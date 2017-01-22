package core

import akka.actor.ActorRef
import akka.util.Timeout
import core.CoreMessage._
import scala.concurrent.duration._

class HyperHost[T <: Host](val host : ActorRef)  {
  implicit val timeout = Timeout(1.second)

  def clientInput(id :String , data: String) = host ! ClientInput(id, data)

  def call(func : T=> Unit ) = {
    host ! Call(func)
  }

}
