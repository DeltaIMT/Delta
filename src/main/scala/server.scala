import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by vannasay on 18/10/16.
  */
class lolPrinter extends Actor {
  override def receive: Receive = {
    case _ => println("lol")
  }
}

object server extends App{
  implicit val actorSystem = ActorSystem("akka-system")
  val actor = actorSystem.actorOf(Props[lolPrinter], "actor")
  actor ! "lol"
  Thread.sleep(1000)
  actorSystem.terminate()
}
