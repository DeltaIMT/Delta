package core.provider

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import core.CoreMessage._
import core.clientView.{ClientViewActor, ClientViewRef}
import core.host.{Host, HostObserver, HostPool}
import core.spatial.Zone
import core.{clientView, observerPattern}
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import ru._

abstract class Provider[ClientViewImpl <: clientView.ClientView : TypeTag : ClassTag] extends Actor {
  var frequency = 10
  val HP0 = HostPool[Host, HostObserver[ClientViewImpl]]
  val HO = HP0.hostObserver
  var clients = collection.mutable.HashMap[String, (observerPattern.Observer, Cancellable)]()
  var clientRef: ActorRef = null
  var providerPort: ActorRef = null
  var portUsed: Int = 0

  override def receive: Receive = {

    case AddClient(id, playerActorRef) => {
      clientRef = playerActorRef
      //Instanciation of ClientView layers
      val clientView = createInstance[ClientViewImpl](id)
      val clientViewActor = context.actorOf(Props(new ClientViewActor(playerActorRef, clientView)), "clientview_" + id)
      val clientViewRef = new ClientViewRef[ClientViewImpl](clientViewActor)

      if (HO != null) HO.call(x => x.id2ClientView += id -> clientViewRef)

      //send the message UpdateClient to the clientViewActor every 100 miliseconds
      val cancellable = context.system.scheduler.schedule(100 milliseconds, (1000 / frequency) milliseconds, clientViewActor, UpdateClient)
      clients += (id -> (new observerPattern.Observer(id, clientViewActor), cancellable))
      OnConnect(id, clients(id)._1)
    }

    case DeleteClient(id) => {
      if (HO != null) HO.call(x => x.id2ClientView -= id)
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

  //checks if the command contains a zone to send to
  //if there is a zone, it sends to all the hosts in the zone the command
  //if not, it sends the command to the hostObserver
  def clientInput(id: String, command: String): Unit = {
    if (command == "ping")
      clientRef ! PlayersUpdate("ping")
    else {
      val jsonObject = Json.parse(command).asInstanceOf[JsArray].value
      jsonObject foreach { j => {
        val hostsZones = hostsStringToZone((j \ "hosts").get.as[String])
        val data = (j \ "data").get.as[String]

        hostsZones match {
          case Some(zone) => {
            val selectedHR = HostPool[Host, HostObserver[_]].getHosts(zone)
            selectedHR.foreach(hostRef => hostRef.clientInput(id, data))
          }
          case None => HP0.hostObserver.clientInput(id, data)
        }
      }
      }
    }
  }

  def OnConnect(id: String, obs: observerPattern.Observer): Unit = {}

  def OnDisconnect(id: String, obs: observerPattern.Observer): Unit = {}

  //create an instance of the type of class provided by the user
  def createInstance[T: TypeTag](arg: Any*): T = {
    createInstance(typeOf[T], arg).asInstanceOf[T]
  }

  def createInstance(tpe: Type, arg: Seq[Any]): Any = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val clsSym = tpe.typeSymbol.asClass
    val clsMirror = mirror.reflectClass(clsSym)
    val ctorSym = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
    val ctorMirror = clsMirror.reflectConstructor(ctorSym)

    val instance = ctorMirror(arg: _*)
    return instance
  }


  //defines the zone where the user wants to send its command
  def hostsStringToZone(s: String): Option[Zone]
}
