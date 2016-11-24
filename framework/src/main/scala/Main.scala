import akka.actor.{ActorSystem, Props}
import core.{FakeClient, HostPool, Provider, Websocket}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import core.`abstract`.UpdateClient
import core.script_filled.{UserClientView, UserHost, UserSpecialHost}



object Main extends App{
  println("framework starting")
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val initialPort = 5000
  val numberOfClient = 100
  val hostsGridWidth = 5
  val hostsGridHeight = 5
  val hostWidth = 200   
  val hostHeight = 200

  val hostPool = new HostPool(hostWidth,hostHeight,hostsGridWidth,hostsGridHeight)
  val hosts =  0 until hostsGridWidth*hostsGridHeight map {i=> actorSystem.actorOf(Props(new UserHost(hostPool)),"host"+i)}
  hostPool.addHost(hosts)
  val specialHost = actorSystem.actorOf(Props(new UserSpecialHost()), "specialHost")

  val providers = 0 until numberOfClient map {i=>actorSystem.actorOf(Props(new Provider(hostPool, specialHost)),"provider_"+i)}
  val websockets = 0 until numberOfClient map {i=>initialPort+i -> new Websocket(providers(i),initialPort+i)}

  println("framework working")

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


  //TEST CLIENT VIEW
  val fakeClient = actorSystem.actorOf(Props(new FakeClient()), "fakeclient")
  val clientViewTest = actorSystem.actorOf(Props(new UserClientView(hostPool,fakeClient)), "clientview")


  for(i <- 0 to 100){

    Thread.sleep(1000)

    clientViewTest ! UpdateClient
  }
//END TEST CLIENT VIEW


  println("framework shutdownn")
  actorSystem.terminate()

}
