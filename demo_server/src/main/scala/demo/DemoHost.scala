package demo

import core.host.{Host, HostPool, HostRef}
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json
import shapeless.get

import scala.util.Random

class DemoHost(zone: SquareZone) extends Host(zone){

  var idMapBalls = collection.mutable.Map[String, Ball]()

  def balls = idMapBalls.values.toList

  override def getViewableFromZone(id : String ,zone: Zone): Iterable[Viewable] = {
    balls.filter(zone.contains(_))
  }

  override def clientInput(id: String, data: String): Unit = {
    val json = Json.parse(data)
    val x = (json \ "x").get.as[Int]
    val y = (json \ "y").get.as[Int]
    val target = Vec(x,y)
    idMapBalls.get(id) match {
      case Some(ball) => ball.speed = (target - ball).normalize()*2
      case None =>
    }

  }

  def tick() = {

    if(idMapBalls.size < 10){
      val id = Random.nextDouble()+""
      idMapBalls  += id ->    new Ball(id,Vec( zone.x + Random.nextInt(zone.w.toInt) , zone.y + Random.nextInt(zone.h.toInt)) )
    }

    balls.foreach(  _.addSpeed )
    balls.combinations(2).foreach {
      case List(a,b)  => {
          if (a.collision(b)){
            if( a.radius > b.radius ) {
              a.eat(b)
              idMapBalls-= b.client
            }
            else{
              b.eat(a)
              idMapBalls-= a.client
            }
          }
      }
    }

    idMapBalls.foreach {
      case (id,ball) => {
        if(!zone.contains(ball)){
          idMapBalls -= id
          Demo.HP.getHost(ball).call( _.idMapBalls += id -> ball)
        }
      }
    }

  }

}
