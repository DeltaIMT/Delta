package stgy

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.stream.ActorMaterializer
import core.CoreMessage.Tick
import core.port_dispatch.ProviderPort
import core.user_import.Zone
import core.{HostPool, Websocket}
import kamon.Kamon
import kamon.metric.instrument.Histogram
import kamon.trace.TraceInfo

import scala.concurrent.duration._

class Suber extends Actor {

  val hash = collection.mutable.HashMap[String, Histogram]()
  override def receive: Receive = {
    case t: TraceInfo => {
      if(hash.contains(t.name))
      hash(t.name).record(t.elapsedTime.nanos)
      else{
        hash += t.name ->  Kamon.metrics.histogram(t.name)
      }
    }
  }
}

object Stgy extends App {
  Kamon.start()
  println("framework starting")
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val executionContext = actorSystem.dispatcher
  implicit val flowMaterializer = ActorMaterializer()
  val initialPort = 9001
  val numberOfClient = 30
  val hostsGridWidth = 5
  val hostsGridHeight = 5
  val hostWidth = 600
  val hostHeight = 600
  val actorRefOfSubscriber = actorSystem.actorOf(Props[Suber], "suber")
  Kamon.tracer.subscribe(actorRefOfSubscriber)
  val hostPool = new HostPool(hostWidth, hostHeight, hostsGridWidth, hostsGridHeight)
  val hosts = 0 until hostsGridWidth * hostsGridHeight map { i => actorSystem.actorOf(Props(new StgyHost(hostPool, new Zone(hostPool.fromI2X(i) * hostWidth, hostPool.fromI2Y(i) * hostHeight, hostWidth, hostHeight))), "host_" + i) }
  hostPool.addHost(hosts)
  val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient)), "providerPort")
  val providerClients = 0 until numberOfClient map { i => actorSystem.actorOf(Props(new StgyProvider(hostPool)), "provider_" + i) }
  val providers = providerPort :: providerClients.toList
  val websockets = -1 until numberOfClient map { i => initialPort + i -> new Websocket(providers(i + 1), initialPort + i) }
  val routes = websockets.map(x => {
    x._1 ->
      (get & parameter("id")) {
        id => handleWebSocketMessages(x._2.flow(id, "region"))
      }
  })
  val interface = "localhost"
  routes foreach { route =>
    Http().bindAndHandle(RouteResult.route2HandlerFlow(route._2), "0.0.0.0", route._1)
  }
  val cancellable = hosts map { h => actorSystem.scheduler.schedule(1000 milliseconds, 16.6 milliseconds, h, Tick) }
  println("framework working")
  import scala.swing._
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
      contents += Button("Shutdown") {shutdown}
      contents += Button("Flush") { hostPool.hyperHostsMap.values foreach( _ method "flush" ) }
      contents += Button("Close") { shutdown;sys.exit(0) }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }
  val ui = new UI
  ui.visible = true
}
