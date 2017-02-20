package demo

import core.host.Host
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json
import jason._

class DemoHost(zone: SquareZone) extends Host(zone) {

  var balls = List[Ball]()

  override def getViewableFromZone(id: String, zone: Zone): Iterable[Viewable] = {
    balls.filter(zone.contains(_))
  }

  override def clientInput(id: String, data: String): Unit = {
//    val json = Json.parse(data)
//    val x = (json \ "x").get.as[Int]
//    val y = (json \ "y").get.as[Int]
//    val tx = (json \ "tx").get.as[Double]
//    val ty = (json \ "ty").get.as[Double]
    val (x, y, tx, ty) = getJ4[Int, Int, Double, Double](data, "x", "y", "tx", "ty")
    val origin = Vec(x, y)
    val speed = Vec(tx, ty)
    balls ::= new Ball(origin, speed)
  }

  def tick() = {
    balls.foreach(_.tick)
    balls.combinations(2).foreach {
      case List(a, b) if a collision b => {
        val speed = a.speed - b.speed
        a.speed -= speed
        b.speed += speed
      }
      case _ =>
    }

    val ballsToRemove = balls.filter(!zone.contains(_))
    ballsToRemove.foreach(ball => {
      Demo.HP.getHost(ball).call(remoteHost => remoteHost.balls ::= ball)
    })
    balls = balls.diff(ballsToRemove)
  }
}
