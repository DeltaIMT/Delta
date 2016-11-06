import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import core.CoreMessage.{AddClient, Command}
import core.{Provider, Server}
import game.GameEvent.Tick
import game.monoActor.{ManagerMonoActor, MonoActor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

class FakeClient(id: String,provider : ActorRef) extends Actor{

  val rand = Random
  override def receive: Receive = {

    case Tick() => provider ! Command(id," {\"mouse\":{\"x\":"+rand.nextInt(6000)+",\"y\":"+rand.nextInt(6000)+"  }  }")

  }
}

object LaunchMonoActor_fake extends App {

  def main = {
    implicit val actorSystem = ActorSystem("akka-system")
    val monoactor = actorSystem.actorOf(Props[MonoActor], "monoactor")
    val manager = actorSystem.actorOf(Props(new ManagerMonoActor(monoactor)), "manager")
    val provider = actorSystem.actorOf(Props(new Provider(manager)), "provider")
    val cancellable = actorSystem.scheduler.schedule(1000 milliseconds, 33.3333 milliseconds, monoactor, Tick())


    new Thread(new Runnable {
      def run() {
        Server.launch(actorSystem, provider)
        cancellable.cancel()
        actorSystem.terminate()
      }}).start()


    Thread.sleep(1000)
    val rand = Random
    val id = 1 to 350
    val idString = id.map(x=>x.toString)
    val fakeClient = idString.map( id => id -> actorSystem.actorOf(Props(new FakeClient(id, provider)), "fc"+id)   )
    Thread.sleep(1000)
    fakeClient.foreach( x => provider ! AddClient(x._1, x._2))
    val canc = fakeClient.map( x => actorSystem.scheduler.schedule(x._1.toInt*20 milliseconds, 33.3333 milliseconds, x._2, Tick()))


  }

  main
}
