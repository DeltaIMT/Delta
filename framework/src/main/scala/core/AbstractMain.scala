package core

//import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import core.CoreMessage.Call
import core.host._
import core.provider.{Provider, ProviderPort}
import kamon.Kamon

import scala.concurrent.duration._
import scala.reflect.runtime.{universe => ru}
import ru._
import scala.concurrent.ExecutionContextExecutor
import scala.reflect.ClassTag


class AbstractMain[
HostImpl <: Host : TypeTag : ClassTag,
ProviderImpl <: Provider[_] : TypeTag : ClassTag,
HostObserverImpl <: HostObserver[_] : TypeTag : ClassTag
] {
  val HP = HostPool[HostImpl,HostObserverImpl]
  var initialPort = 9001
  var numberOfClient = 30

  implicit var actorSystem: ActorSystem=_
  implicit var executionContext:ExecutionContextExecutor=_

  def setInterval( hr : HostRef[HostImpl] , time : Int, func : HostImpl => Unit ) = {
    actorSystem.scheduler.schedule(1000 milliseconds, time milliseconds, hr.actor , Call(func)  )
  }

  def launch(hosts : Iterable[HostImpl], hostObserver : HostObserverImpl) = {
    println("framework starting")
    actorSystem = ActorSystem("akka-system")
    executionContext = actorSystem.dispatcher
    implicit val flowMaterializer = ActorMaterializer()


     hosts.foreach( h => {
      val actor = actorSystem.actorOf(Props(new HostActor[HostImpl](h)))
      val ref = new HostRef[HostImpl](actor)
      HP.hosts +=  h.zone -> ref
    })

    val hostObserverActor = actorSystem.actorOf(Props(new HostActor[HostObserverImpl](hostObserver)))
    HP.hostObserver = new HostRef[HostObserverImpl]( hostObserverActor)

    val providerClients = 0 until numberOfClient - 1 map
      { i => actorSystem.actorOf(Props(createInstance[ProviderImpl]()), "provider_" + i) }
    val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient, providerClients)), "providerPort")
    val providers = providerPort :: providerClients.toList
    val websockets = -1 until numberOfClient - 1 map
      { i => initialPort + i -> new Websocket(providers(i + 1), initialPort + i,flowMaterializer) }

    val routes = websockets.map {
      case (port, ws) => {
        port->
          (get & parameter("id")) {
            id => handleWebSocketMessages(ws.flow(id))
          }
      }
    }

    routes foreach { case (port,route) =>
      Http().bindAndHandle(RouteResult.route2HandlerFlow(route), "0.0.0.0", port)
    }

    println("framework working")
  }


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


}


