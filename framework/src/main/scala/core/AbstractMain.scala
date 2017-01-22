package core

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import core.CoreMessage.Call
import core.`abstract`.ContainerHost
import core.port_dispatch.ProviderPort
import core.user_import.Zone
import kamon.Kamon
import kamon.metric.instrument.Histogram
import kamon.trace.TraceInfo
import stgy.StgyProvider

import scala.concurrent.duration._
import scala.reflect.runtime.{universe => ru}
import ru._
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.reflect.ClassTag


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


class AbstractMain[HostType <: Host : TypeTag : ClassTag, ProviderType <: Provider[_]  : TypeTag : ClassTag] {
  val initialPort = 9001
  val numberOfClient = 30

  Kamon.start()
  println("framework starting")
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val executionContext = actorSystem.dispatcher
  implicit val flowMaterializer = ActorMaterializer()
  val hostsGridWidth = 5
  val hostsGridHeight = 5
  val hostWidth = 600
  val hostHeight = 600
  val actorRefOfSubscriber = actorSystem.actorOf(Props[Suber], "suber")
  val hostPool = new HostPool[HostType](hostWidth, hostHeight, hostsGridWidth, hostsGridHeight)
  val hosts = 0 until hostsGridWidth * hostsGridHeight map { i => {
    val zone = new Zone(hostPool.fromI2X(i) * hostWidth, hostPool.fromI2Y(i) * hostHeight, hostWidth, hostHeight)
    val inside = createInstance[HostType](hostPool, zone)
    actorSystem.actorOf(Props(new ContainerHost(hostPool, zone, inside)), "host_" + i)
  }
  }
  Kamon.tracer.subscribe(actorRefOfSubscriber)
  val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient)), "providerPort")
  val providerClients = 0 until numberOfClient map { i => actorSystem.actorOf(Props(createInstance[ProviderType](hostPool) ), "provider_" + i) }
  hostPool.addHost(hosts)
  val providers = providerPort :: providerClients.toList
  val websockets = -1 until numberOfClient map { i => initialPort + i -> new Websocket(providers(i + 1), initialPort + i) }
  val routes = websockets.map(x => {
    x._1 ->
      (get & parameter("id")) {
        id => handleWebSocketMessages(x._2.flow(id, "region"))
      }
  })
  val interface = "localhost"
  val cancellable = hosts map { h => actorSystem.scheduler.schedule(1000 milliseconds, 16.6 milliseconds, h, Call((x: Host) => x.tick())) }
  val ui = new UI
  routes foreach { route =>
    Http().bindAndHandle(RouteResult.route2HandlerFlow(route._2), "0.0.0.0", route._1)
  }

  def createInstance[T: TypeTag](arg: Any*): T = {
    createInstance(typeOf[T], arg).asInstanceOf[T]
  }

  println("framework working")

  import scala.swing._

  def createInstance(tpe: Type, arg: Seq[Any]): Any = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    val clsSym = tpe.typeSymbol.asClass
    val clsMirror = mirror.reflectClass(clsSym)
    val ctorSym = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
    val ctorMirror = clsMirror.reflectConstructor(ctorSym)

    val instance = ctorMirror(arg: _*)
    return instance
  }

  class UI extends MainFrame {
    title = "GUI for Delta Server"

    def shutdown = {
      println("framework shutdown")
      cancellable foreach { c => c.cancel() }
      actorSystem.terminate()
      Kamon.shutdown()
      println("Done")
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Server")
      contents += Swing.VStrut(10)
      contents += Swing.Glue
      contents += Button("Shutdown") {
        shutdown
      }
      //  contents += Button("Flush") { hostPool.hyperHostsMap.values foreach( _.call( i => i.flush() )) }
      contents += Button("Close") {
        shutdown;
        sys.exit(0)
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }

  ui.visible = true


}
