package core

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import core.CoreMessage.{Call, CallTrace}
import core.`abstract`.{ContainerHost, ContainerHostObserver}
import core.port_dispatch.ProviderPort
import core.user_import.Zone
import kamon.Kamon
import kamon.metric.instrument.Histogram
import kamon.trace.TraceInfo

import scala.reflect.runtime.{universe => ru}
import ru._
import scala.collection.immutable.IndexedSeq
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.swing._

class Suber extends Actor {

  val hash = collection.mutable.HashMap[String, Histogram]()

  override def receive: Receive = {
    case t: TraceInfo => {
      if (hash.contains(t.name))
        hash(t.name).record(t.elapsedTime.nanos)
      else {
        hash += t.name -> Kamon.metrics.histogram(t.name)
      }
    }
  }
}


class AbstractMain[HostType <: Host : TypeTag : ClassTag, ProviderType <: Provider[_,_] : TypeTag : ClassTag, HostObserverType <: HostObserver : TypeTag : ClassTag] {
  var initialPort = 9001
  var numberOfClient = 30
  var hostsGridWidth = 5
  var hostsGridHeight = 5
  var hostWidth = 600
  var hostHeight = 600


  var cancellable: Seq[Cancellable]=_
  implicit var actorSystem: ActorSystem=_
  var hostPool: HostPool[HostType, HostObserverType] = _

  def launch = {
    println("framework starting")
    Kamon.start()
    actorSystem = ActorSystem("akka-system")
    implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
    implicit val flowMaterializer = ActorMaterializer()

    val actorRefOfSubscriber: ActorRef = actorSystem.actorOf(Props[Suber], "suber")
    hostPool = new HostPool[HostType, HostObserverType](hostWidth, hostHeight, hostsGridWidth, hostsGridHeight)
    val hosts: IndexedSeq[ActorRef] = 0 until hostsGridWidth * hostsGridHeight map { i => {
      val zone = new Zone(hostPool.fromI2X(i) * hostWidth, hostPool.fromI2Y(i) * hostHeight, hostWidth, hostHeight)
      val inside = createInstance[HostType](hostPool, zone)
      actorSystem.actorOf(Props(new ContainerHost(hostPool, zone, inside)), "host_" + i)
    }
    }

    val hostObserver = createInstance[HostObserverType](hostPool)
    val containerHostObserver = actorSystem.actorOf(Props(new ContainerHostObserver[HostType, HostObserverType](hostPool, hostObserver)))
    val hyperHostObserver = new HyperHostObserver[HostObserverType](containerHostObserver)

    val providerClients = 0 until numberOfClient - 1 map { i => actorSystem.actorOf(Props(createInstance[ProviderType](hostPool, hyperHostObserver)), "provider_" + i) }
    val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient, providerClients)), "providerPort")
    Kamon.tracer.subscribe(actorRefOfSubscriber)
    val providers = providerPort :: providerClients.toList
    val websockets = -1 until numberOfClient - 1 map { i => initialPort + i -> new Websocket(providers(i + 1), initialPort + i,flowMaterializer) }

    hostPool.addHost(hosts)
    hostPool.setHyperHostObserver(hyperHostObserver)

    val routes = websockets.map(x => {
      x._1 ->
        (get & parameter("id")) {
          id => handleWebSocketMessages(x._2.flow(id, "region"))
        }
    })

    routes foreach { route =>
      Http().bindAndHandle(RouteResult.route2HandlerFlow(route._2), "0.0.0.0", route._1)
    }

   // cancellable = hosts map { h => actorSystem.scheduler.schedule(1000 milliseconds, 16.6 milliseconds, h, CallTrace((x: Host) => x.tick(),"tick")) }

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
