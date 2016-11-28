package core.`abstract`

import akka.actor.{Actor, ActorRef, Props}
import core.CoreMessage.{AddClient, ConnectClient, DeleteClient, PlayersUpdate}
import core.HostPool

import scala.reflect.runtime._
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{universe => ru}
import ru._
import scala.reflect.ClassTag

class AbstractSpecialHost[T<:AbstractClientViewWorker : TypeTag](val hostPool: HostPool) extends Actor{

  var clients = collection.mutable.HashMap[String,ActorRef]()
  var idClient = 0

  def OnConnect(client: ActorRef) : Unit = {}


  override def receive: Receive = {
    case AddClient(id, clientActorRef) => {
      val clientView = context.actorOf(Props(new AbstractClientView[T](hostPool, clientActorRef)))
      clients += (id -> clientView)
      OnConnect(clientView)
      clientActorRef ! PlayersUpdate("you are connected")
    }

    case DeleteClient(id) => {
      clients-= id
    }

    case _ => {}
  }
}
