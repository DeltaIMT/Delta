import akka.actor.{ActorSystem, Props}
import core.{Provider, Server}
import game.GameEvent.Tick
import game.MonoActor.{ManagerMonoActor, MonoActor}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object LaunchMonoActor extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  val monoactor  = actorSystem.actorOf(Props[MonoActor], "monoactor")
  val manager  = actorSystem.actorOf(Props(new ManagerMonoActor(monoactor)), "manager")
  val provider  = actorSystem.actorOf(Props(new Provider(manager)), "provider")
  val cancellable  = actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, monoactor, Tick())
  Server.launch(actorSystem,provider)
  cancellable.cancel()
  actorSystem.terminate()
}
