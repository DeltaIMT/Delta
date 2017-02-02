package core2

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import core.CoreMessage.{FromProviderPort, _}
import core.`abstract`.UpdateClient
import core2.clientView.ClientViewRef
import core2.host.{Host, HostObserver}
import core2.spatial.Zone
import play.api.libs.json.{JsArray, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import ru._

abstract class Provider[ClientViewImpl <: clientView.ClientView : TypeTag : ClassTag] extends Actor {

  var clients = collection.mutable.HashMap[String, (observerPattern.Observer, Cancellable)]()
  var clientRef: ActorRef = null
  var providerPort: ActorRef = null
  var portUsed: Int = 0

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      val clientView = context.actorOf(Props(createInstance[ClientViewImpl](playerActorRef).asInstanceOf[ClientViewImpl]), "clientview_" + id)
      val clientViewRef = new ClientViewRef[ClientViewImpl](clientView)
      HostPool[_, HostObserver[ClientViewImpl]].hostsObserver.call(x => x.id2ClientView += id -> clientViewRef)
      val cancellable = context.system.scheduler.schedule(100 milliseconds, 100 milliseconds, clientView, UpdateClient)
      clients += (id -> (new observerPattern.Observer(id, clientView), cancellable))
      OnConnect(id, clients(id)._1)
    }

    case DeleteClient(id) => {
      HostPool[_, HostObserver[ClientViewImpl]].hostsObserver.call(x => x.id2ClientView -= id)
      clients(id)._2.cancel()
      OnDisconnect(id, clients(id)._1)
      clients -= id
      providerPort ! ClientDisconnection(portUsed)
    }

    case x: ClientInputWithLocation => {
      clientInput(x.id, x.command)
    }

    case FromProviderPort(provPort, port) => {
      providerPort = provPort
      portUsed = port
    }

    case _ => {}
  }

  def clientInput(id: String, command: String): Unit = {
    if (command == "ping") {
      clientRef ! PlayersUpdate("ping")
    }

    else {
      val jsonObject = Json.parse(command).asInstanceOf[JsArray].value
      jsonObject foreach { j => {
        val hostsZones = hostsStringToZone((j \ "hosts").get.as[String])
        val data = (j \ "data").get.as[String]
        HostPool[Host, _].getHosts(hostsZones).foreach(hostRef => hostRef.clientInput(id, data))
      }
      }
    }
  }

  def OnConnect(id: String, obs: observerPattern.Observer): Unit = {}

  def OnDisconnect(id: String, obs: observerPattern.Observer): Unit = {}

  def createInstance[T: TypeTag](ar: ActorRef): Any = {
    createInstance(typeOf[T], ar)
  }

  def createInstance(tpe: Type, ar: ActorRef): Any = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val clsSym = tpe.typeSymbol.asClass
    val clsMirror = mirror.reflectClass(clsSym)
    val ctorSym = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
    val ctorMirror = clsMirror.reflectConstructor(ctorSym)
    val instance = ctorMirror(ar)
    return instance
  }

  def hostsStringToZone(s: String): Zone
}
