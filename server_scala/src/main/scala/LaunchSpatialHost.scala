import akka.actor.{ActorRef, ActorSystem, Props}
import core.{Provider, Server}
import game.GameEvent.Tick
import game.spatialHost.{ManagerSpatialHost, SpatialHost, OtherSpatial}
import game.GameEvent.Vector
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object LaunchSpatialHost extends App {
  implicit val actorSystem = ActorSystem("akka-system")

  val spatialA  = actorSystem.actorOf(Props(new SpatialHost(Vector(0,0),Vector(500,1000),1)), "spatialA")
  val spatialB  = actorSystem.actorOf(Props(new SpatialHost(Vector(500,0),Vector(500,1000),2)), "spatialB")

  spatialA ! OtherSpatial(spatialB)
  spatialB ! OtherSpatial(spatialA)

  var listHost = List[ActorRef]()
  listHost  = spatialA :: listHost
  listHost  = spatialB :: listHost

  val manager  = actorSystem.actorOf(Props(new ManagerSpatialHost(listHost)), "manager")
  val provider  = actorSystem.actorOf(Props(new Provider(manager)), "provider")

  val cancellableA  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, spatialA, Tick())
  val cancellableB  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, spatialB, Tick())

  Server.launch(actorSystem,provider)

  cancellableA.cancel()
  cancellableB.cancel()
  actorSystem.terminate()
}
