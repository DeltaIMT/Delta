import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.server.Directives
import scala.io.StdIn
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Props
import scala.concurrent.duration._
import play.api.libs.json._



object server extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  val region  = actorSystem.actorOf(Props[Region], "region")
  val region1 = actorSystem.actorOf(Props[Region], "region1")
  val cancellable  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, region, Tick())
  val cancellable1  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, region1, Tick())
  val regionMap = Map[String, ActorRef](("region", region), ("region1", region1))

  import Directives._
  val ws = new WebSocket(regionMap)
  val route = (get & parameter("id") ){id =>  handleWebSocketMessages(ws.flow(id, "region"))}
  val interface ="localhost"
  val port = 8080
  val binding = Http().bindAndHandle(route, "0.0.0.0", port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()
  cancellable.cancel()
  cancellable1.cancel()
  actorSystem.terminate()
  println("Server is down...")
}


