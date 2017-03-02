package splash

import core.host.{Host, HostPool, HostRef}
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json

class SplatHost(zone: SquareZone, val pos: Int) extends Host(zone){
  val HP = HostPool[SplatHost, SplatHostObserver]

  var balls = List[Ball]()
  var players = collection.mutable.Map[String, Shooter]()
  var neighbours = List[HostRef[SplatHost]]()

  var cases = collection.mutable.Map[(Int,Int), Case]()
  val width = 150
  val height = 150


  (0 to 15).map(i => {
    val x = (i%4 * width) + zone.x
    val y = (i/4 * height) + zone.y
    val c = new Case(x, y, width, height, 2)
    cases += (x.toInt,y.toInt) -> c
  })

  def kill(id : String) ={
    if (players.contains(id))
    players -= id
  }

  override def getViewableFromZone(id : String ,zone: Zone): Iterable[Viewable] = {
    val caseToSend = cases.filter(c => zone.contains(c._2) && !c._2.hasItBeenSeenBy(id)  ).values
    caseToSend.foreach( c => c.hasBeenSeenBy(id))
    players.values ++ caseToSend ++ balls.filter( zone.contains(_))
  }

  override def clientInput(id: String, data: String): Unit = {
    val json = Json.parse(data)
    val keydown = (json \ "keydown").toOption
    val keyup = (json \ "keyup").toOption
    val x = (json \ "x").toOption
    val y = (json \ "y").toOption
    val tx = (json \ "tx").toOption
    val ty = (json \ "ty").toOption

    if(players.contains(id)) {
      if(x.isDefined && y.isDefined && (players(id).hit == 0)){
        val shootX = x.get.as[Int]
        val shootY = y.get.as[Int]
        val player = players(id)
        val v = Vec( shootX-player.x , shootY-player.y).normalize()*6
        val ball = new Ball(player.x,player.y,v.x,v.y,shootX,shootY, player.color, player.clientId)
        balls = ball :: balls
      }

      if (keydown.isDefined) {
        val letter = keydown.get.as[String]
        letter match {
          case "z" => players(id).z = true
          case "s" => players(id).s = true
          case "q" => players(id).q = true
          case "d" => players(id).d = true
          case "space" => players(id).space = true
        }
      }

      if (keyup.isDefined) {
        val letter = keyup.get.as[String]
        letter match {
          case "z" => players(id).z = false
          case "s" => players(id).s = false
          case "q" => players(id).q = false
          case "d" => players(id).d = false
          case "space" => players(id).space = false
        }
      }

      if (tx.isDefined && ty.isDefined && players(id).space) {
        val toX = tx.get.as[Int]
        val toY = ty.get.as[Int]
        HP.hostObserver.call(_.teleportPlayer(players(id), toX, toY))
      }
    }
  }

  def tick() = {

    players.values.foreach(p => {
      if(p.z)
        p.y -=3
      if(p.s)
        p.y +=3
      if(p.q)
        p.x -=3
      if(p.d)
        p.x +=3
    })

    players.foreach(p => {
      if(!zone.contains(p._2)){
        HP.getHost(p._2).call(_.addPlayer(p._2.asInstanceOf[Shooter]))
        players -= p._1
      }
    })

    balls.foreach(b => {
      b.step
      if(!zone.contains(b)){
        HP.getHost(b).call(_.addBall(b))
        balls = balls.filter(_!=b)
      }
    })

    balls.foreach(b => {
      players.filter(p => p._1 != b.shooterId).values.foreach(s => {
        if(((Vec(b.x,b.y)-Vec(s.x, s.y)).length() < 20) && !b.hasHitPlayer){
          if (b.color != s.color) {
            s.hit = 1
          }
          else {
            s.hit = 0
          }
          b.hasHitPlayer = true
        }
      })
    })

    balls.foreach( b => {
      if (b.lifeTime <= 0){
        if (!b.hasHitPlayer){
          val x=  Math.floor(b.x/width)*width
          val y=  Math.floor(b.y/height)*height
          val xy = (x.toInt,y.toInt)
          cases(xy).changeColor(b.color)
          HP.hostObserver.call(_.changeCaseColor(cases(xy)))
        }
        balls = balls.filter( _!=b)
      }
    })
  }

  def addPlayer(p: Shooter): Unit = {
    players += p.clientId -> p
  }

  def addBall(b: Ball): Unit ={
    balls = b :: balls
  }
}
