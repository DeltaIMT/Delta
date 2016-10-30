import akka.actor.{ActorSystem, Props}
import core.{Provider, Server}
import game.GameEvent.Tick
import game.actorPerPlayer.ManagerActorPerPlayer
import game.monoActor.{ManagerMonoActor, MonoActor}

/**
  * Created by thoma on 30/10/2016.
  */
object LaunchActorPerPlayer extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  val manager  = actorSystem.actorOf(Props(new ManagerActorPerPlayer()), "manager")
  val provider  = actorSystem.actorOf(Props(new Provider(manager)), "provider")
  Server.launch(actorSystem,provider)
  actorSystem.terminate()
}
