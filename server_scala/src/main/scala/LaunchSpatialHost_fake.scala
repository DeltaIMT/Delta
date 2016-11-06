import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import core.CoreMessage.{AddClient, Command, SetProvider}
import core.{Provider, Server}
import game.GameEvent.Tick
import game.spatialHost._
import game.GameEvent.Vector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random


class FakeClient(id: String,provider : ActorRef) extends Actor{

  val rand = Random
  override def receive: Receive = {

    case Tick() => provider ! Command(id," {\"mouse\":{\"x\":"+rand.nextInt(6000)+",\"y\":"+rand.nextInt(6000)+"  }  }")

  }
}



object LaunchSpatialHost_fake extends App {


  def main() = {
    implicit val actorSystem = ActorSystem("akka-system")

    val width = 5
    val height = 5
    def x_i(i: Int) = i % width
    def y_i(i: Int) = i / width
    def i_xy(x: Int, y: Int) = if (x >= 0 && x < width && y >= 0 && y < height) x + y * width else -1
    var listHost = List[ActorRef]();
    for (i <- 0 until width * height) {
      var j = width * height - 1 - i
      val spatial = actorSystem.actorOf(Props(new SpatialHost(Vector(1000 * x_i(j), 1000 * y_i(j)), Vector(1000, 1000), 1)), "spatial" + i)
      listHost = spatial :: listHost
    }

    for (i <- 0 until width * height) {
      var j = 0

      j = i_xy(x_i(i), y_i(i) + 1)
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "N")
      j = i_xy(x_i(i) + 1, y_i(i) + 1)
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "NE")
      j = i_xy(x_i(i) + 1, y_i(i))
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "E")
      j = i_xy(x_i(i) + 1, y_i(i) - 1)
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "SE")
      j = i_xy(x_i(i), y_i(i) - 1)
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "S")
      j = i_xy(x_i(i) - 1, y_i(i) - 1)
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "SW")
      j = i_xy(x_i(i) - 1, y_i(i))
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "W")
      j = i_xy(x_i(i) - 1, y_i(i) + 1)
      if (j != (-1))
        listHost(i) ! OtherSpatial(listHost(j), "NW")
    }

    //  listHost(5) ! SayPosAll()

    val manager = actorSystem.actorOf(Props(new ManagerSpatialHost(listHost)), "manager")
    val provider = actorSystem.actorOf(Props(new Provider(manager)), "provider")

    listHost.foreach(x => x ! SetProvider(provider))
    listHost.foreach(x => x ! SetReadList(listHost))
    val cancellableI = listHost.map(x => actorSystem.scheduler.schedule(1000 milliseconds, 33.3333 milliseconds, x, Tick()))

    new Thread(new Runnable {
      def run() {
        Server.launch(actorSystem, provider)
        cancellableI.foreach(x => x.cancel())
        actorSystem.terminate()
      }
    }).start()

    Thread.sleep(1000)
    val rand = Random
    val id = 1 to 200
    val idString = id.map(x=>x.toString)
    val fakeClient = idString.map( id => id -> actorSystem.actorOf(Props(new FakeClient(id, provider)), "fc"+id)   )
    Thread.sleep(1000)
    fakeClient.foreach( x => provider ! AddClient(x._1, x._2))
    val canc = fakeClient.map( x => actorSystem.scheduler.schedule(1000 milliseconds, 33.3333 milliseconds, x._2, Tick()))

  }
  main
}