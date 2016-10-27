import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import core.{Provider, WebSocket}
import game.GameEvent.Tick
import game.MonoActor.MonoActor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn

object server extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val manager  = actorSystem.actorOf(Props[MonoActor], "manager")
  val provider  = actorSystem.actorOf(Props(new Provider(manager)), "provider")
  val monoactor  = actorSystem.actorOf(Props[MonoActor], "monoactor")
  val cancellable  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, monoactor, Tick())
  import Directives._
  val ws = new WebSocket(provider)
  val route = (get & parameter("id") ){id =>  handleWebSocketMessages(ws.flow(id, "region"))}
  val interface ="localhost"
  val port = 8080
  val binding = Http().bindAndHandle(route, "0.0.0.0", port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()
  cancellable.cancel()
  actorSystem.terminate()
  println("Server is down...")
}


