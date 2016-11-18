import akka.actor.{ActorSystem, Props}
import core.script_filled.UserHost
import core.{Provider, Websocket}
import kamon.Kamon


object Main extends App{

  implicit val actorSystem = ActorSystem("akka-system")

  val initialPort = 5000
  val numberOfClient = 100
  val hostsGridWidth= 5
  val hostsGridHeight= 5

  println("lolipop")

  val hosts =  0 until hostsGridWidth*hostsGridHeight map {i=> actorSystem.actorOf(Props(new UserHost()),"host"+i)}
  val providers = 0 until numberOfClient map {i=>actorSystem.actorOf(Props(new Provider(hosts)),"provider_"+i)}
  val websockets = 0 until numberOfClient map {i=>new Websocket(providers(i),initialPort+i)}


  actorSystem.terminate()

}
