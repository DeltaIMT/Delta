package core

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import core.CoreMessage.{FromProviderPort, _}
import core.`abstract`.{AbstractClientView, UpdateClient}
import core.user_import.Observer
import play.api.libs.json.{JsArray, Json}

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import ru._
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class Provider[T <: AbstractClientView : TypeTag : ClassTag](hosts: HostPool[_]) extends Actor {

  var clients = collection.mutable.HashMap[String, (Observer, Cancellable)]()
  var clientRef: ActorRef = null
  var providerPort:ActorRef = null
  var portUsed:Int = 0

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      val clientView = context.actorOf(Props(createInstance[T](playerActorRef).asInstanceOf[T]) , "clientview_" + id)
      val cancellable = context.system.scheduler.schedule(100 milliseconds, 100 milliseconds, clientView, UpdateClient)
      clients += (id -> (new Observer(id, clientView), cancellable))
      OnConnect(id, clients(id)._1)
//      println("Provider Connection    " + id)
    }

    case DeleteClient(id) => {
      clients(id)._2.cancel()
      OnDisconnect(id, clients(id)._1)
      clients -= id
//      println("Provider Disconnection " + id)
      providerPort ! ClientDisconnection(portUsed)
    }

    case x: ClientInputWithLocation => {
      if (x.command == "ping") {
        clientRef ! PlayersUpdate("ping")
      }

      else {
        val jsonObject = Json.parse(x.command).asInstanceOf[JsArray].value
        jsonObject foreach { j => {
          val hosts1 = (j \ "hosts").get.as[JsArray].value
          val hosts2 = hosts1 map { x => x.as[JsArray].value }
          val hosts = hosts2 map { x => x map { y => y.as[Double] } }
          val data = (j \ "data").get.as[String]
          hosts foreach { h => this.hosts.getHyperHost(h(0), h(1)).host ! ClientInput(x.id, data) }
        }
        }
      }

    }
    case FromProviderPort(provPort, port) => {
      providerPort = provPort
      portUsed = port
    }



    case _ => {}
  }

  def OnConnect(id: String, obs: Observer): Unit = {}

  def OnDisconnect(id: String, obs: Observer): Unit = {}

  def createInstance[T: TypeTag](ar: ActorRef): Any = {
    createInstance(typeOf[T], ar)
  }

  def createInstance(tpe: Type, ar: ActorRef): Any = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val clsSym = tpe.typeSymbol.asClass
    val clsMirror = mirror.reflectClass(clsSym)
    val ctorSym = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
    val ctorMirror = clsMirror.reflectConstructor(ctorSym)
    val instance = ctorMirror(hosts, ar)
    return instance
  }
}
