package core.user_import

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import core.UserClientView
import org.scalatest.FunSuite
class ObservableTest extends FunSuite {


  class ObsClass extends Observable{}

  object testObservable extends App{


    println("framework starting")
      implicit val actorSystem = ActorSystem("akka-system")
      implicit val flowMaterializer = ActorMaterializer()
      val initialPort = 5000


    var observable = new ObsClass()

    var u1 = actorSystem.actorOf(Props(new UserClientView(null,null)), "clientView1")
    var u2 = actorSystem.actorOf(Props(new UserClientView(null,null)), "clientView2")
    observable.sub(u1)
    observable.sub(u2)
    observable.notifyClientViews
    observable.unSub(u2)
    observable.notifyClientViews


    println("framework shutdownn")
    actorSystem.terminate()

  }




}
