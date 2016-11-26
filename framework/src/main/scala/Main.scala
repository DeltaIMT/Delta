import akka.actor.{ActorSystem, Props}
import core.{FakeClient, HostPool, Provider, Websocket}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import core.CoreMessage.AddClient
import core.port_dispatch.ProviderPort
import core.script_filled.{UserHost, UserSpecialHost}
import core.port_dispatch.ProviderPort


object Main extends App{
  println("framework starting")
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val initialPort = 9001
  val numberOfClient = 100
  val hostsGridWidth = 5
  val hostsGridHeight = 5
  val hostWidth = 200   
  val hostHeight = 200

  val hostPool = new HostPool(hostWidth,hostHeight,hostsGridWidth,hostsGridHeight)
  val hosts =  0 until hostsGridWidth*hostsGridHeight map {i=> actorSystem.actorOf(Props(new UserHost(hostPool)),"host"+i)}
  hostPool.addHost(hosts)
  val specialHost = actorSystem.actorOf(Props(new UserSpecialHost(hostPool)), "specialHost")

  val providerPort = actorSystem.actorOf(Props(new ProviderPort(numberOfClient)),"providerPort")
  val providerClients = 0 until numberOfClient map {i=>actorSystem.actorOf(Props(new Provider(hostPool, specialHost)),"provider_"+i)}
  val providers = providerPort :: providerClients.toList
  val websockets = -1 until numberOfClient map {i=>initialPort+i -> new Websocket(providers(i+1),initialPort+i)}

  val routes = websockets.map(x => {
    x._1 ->
      (get & parameter("id") ){
        id =>  handleWebSocketMessages(x._2.flow(id, "region"))
      }
  })

  val interface ="localhost"

  routes foreach { route =>
    Http().bindAndHandle(route._2, "0.0.0.0", route._1)
  }
  println("framework working")

  Thread.sleep(1000000)
  println("framework shutdownn")
  actorSystem.terminate()

}
