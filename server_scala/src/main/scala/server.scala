import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import core.WebSocket
import game.GameEvent.Tick
import game.MonoActor.MonoActor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn

object server extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val region  = actorSystem.actorOf(Props[MonoActor], "region")
  val cancellable  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, region, Tick())
  import Directives._
  val ws = new WebSocket(region)
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


