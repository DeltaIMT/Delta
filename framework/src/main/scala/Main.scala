import akka.actor.{ActorSystem, Props}
import core.script_filled.UserHost
import core.{HostPool, Provider, Websocket}



object Main extends App{
  println("framework starting")
  implicit val actorSystem = ActorSystem("akka-system")

  val initialPort = 5000
  val numberOfClient = 100
  val hostsGridWidth= 5
  val hostsGridHeight= 5
  val hostWidth = 200
  val hostHeight = 200

  val hosts =  0 until hostsGridWidth*hostsGridHeight map {i=> actorSystem.actorOf(Props(new UserHost()),"host"+i)}
  val hostPool = new HostPool(hostWidth,hostHeight,hostsGridWidth,hostsGridHeight, hosts)
  val providers = 0 until numberOfClient map {i=>actorSystem.actorOf(Props(new Provider(hostPool)),"provider_"+i)}
  val websockets = 0 until numberOfClient map {i=>new Websocket(providers(i),initialPort+i)}
  println("framework working")

  println("framework shutdownn")
  actorSystem.terminate()

}
