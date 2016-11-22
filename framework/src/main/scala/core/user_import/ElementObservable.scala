package core.user_import

import core.script_filled.UserClientView
import akka.actor.{ActorSystem, Props}
import core.{HostPool, Provider, Websocket}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import core.script_filled.{UserHost, UserSpecialHost}
import core.{Provider, Websocket}


/**
  * Created by Charly on 18/11/2016.
  */
object testObservable extends App{


  println("framework starting")
    implicit val actorSystem = ActorSystem("akka-system")
    implicit val flowMaterializer = ActorMaterializer()
    val initialPort = 5000


  var observable = new Observable()

  var u1 = actorSystem.actorOf(Props(new UserClientView()), "clientView1")
  var u2 = actorSystem.actorOf(Props(new UserClientView()), "clientView2")
  observable.sub(u1)
  observable.sub(u2)
  observable.notifyClientViews
  observable.unSub(u2)
  observable.notifyClientViews


  println("framework shutdownn")
  actorSystem.terminate()

}
