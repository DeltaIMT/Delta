import akka.actor.{ActorSystem, Props}
import core.{Provider, Server}
import game.GameEvent.Tick
import game.actorPerPlayer.ManagerActorPerPlayer
import game.monoActor.{ManagerMonoActor, MonoActor}


object LaunchActorPerPlayer extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  val manager  = actorSystem.actorOf(Props(new ManagerActorPerPlayer()), "manager")
  val provider  = actorSystem.actorOf(Props(new Provider(manager)), "provider")
  Server.launch(actorSystem,provider)
  actorSystem.terminate()
}
