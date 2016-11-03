import akka.actor.{ActorRef, ActorSystem, Props}
import core.CoreMessage.SetProvider
import core.{Provider, Server}
import game.GameEvent.Tick
import game.spatialHost.{ManagerSpatialHost, OtherSpatial, SayPosAll, SpatialHost}
import game.GameEvent.Vector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object LaunchSpatialHost extends App {
  implicit val actorSystem = ActorSystem("akka-system")

  val width= 3
  val height = 3
  def x_i(i:Int) = i %width
  def y_i(i:Int) = i /width
  def i_xy(x:Int,y:Int) =if (x>=0 && x<width  && y>=0 && y<height)   x+y*width else -1
  var listHost = List[ActorRef]();
  for(i <- 0 until width*height ){
    var j = width*height-1 - i
    val spatial  = actorSystem.actorOf(Props(new SpatialHost(Vector(250*x_i(j),250*y_i(j)),Vector(250,250),(i%2)+1 )), "spatial"+i)
    listHost = spatial ::listHost
  }

  for(i <- 0 until width*height ){
    var j=0

    j = i_xy(x_i(i),y_i(i)+1)
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j ),"N")
    j = i_xy(x_i(i)+1,y_i(i)+1)
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j ),"NE")
    j = i_xy(x_i(i)+1,y_i(i))
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j ),"E")
    j = i_xy(x_i(i)+1,y_i(i)-1)
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j ),"SE")
    j = i_xy(x_i(i),y_i(i) -1)
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j),"S")
    j = i_xy(x_i(i)-1,y_i(i)-1)
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j),"SW")
    j = i_xy(x_i(i)-1,y_i(i))
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j),"W")
    j = i_xy(x_i(i)-1,y_i(i)+1)
    if (j>=0 && j<=8 )
    listHost(i) ! OtherSpatial(listHost(j ),"NW")
  }

  listHost(4) ! SayPosAll()

  val manager  = actorSystem.actorOf(Props(new ManagerSpatialHost(listHost)), "manager")
  val provider  = actorSystem.actorOf(Props(new Provider(manager)), "provider")

  listHost.foreach( x => x ! SetProvider(provider))
  val cancellableI = listHost.map( x => actorSystem.scheduler.schedule( 1000 milliseconds , 33.3333 milliseconds, x, Tick()) )
  Server.launch(actorSystem,provider)
  cancellableI.foreach( x => x.cancel())
  actorSystem.terminate()
}
