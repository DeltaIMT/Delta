package core
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import scala.io.StdIn

object Server{
  def launch(provider : ActorRef) = {
    import Directives._
    val ws = new WebSocket(provider)
    val route = (get & parameter("id") ){id =>  handleWebSocketMessages(ws.flow(id, "region"))}
    val interface ="localhost"
    val port = 8080
    val binding = Http().bindAndHandle(route, "0.0.0.0", port)
    println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
    StdIn.readLine()
    println("Server is down...")
  }
}


