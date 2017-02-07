package championship

import core.observerPattern.Observable

import scala.util.Random

class Splash(var x : Double, var y : Double, var id : String)  extends Element  {
  var lifeLeft = 30
  var listOfKnownClient: List[String] = List[String]()
  def step : Boolean = {
    lifeLeft-= 1
    lifeLeft <= 0
  }
}

class Boat(var x: Double, var y: Double, var id: String) extends Element with Observable {

  var size = 0.0
  var health = (size * 2.0, size * 2.0)
  var speed = Vec(0, 0)

  var clientId2position = collection.mutable.HashMap[String, Position]()
  var position2moveTargets = collection.mutable.HashMap[Position, Option[Vec]]()
  var position2shoots = collection.mutable.HashMap[Position, Boolean]()
  var position2firedelay = collection.mutable.HashMap[Position, Int]()
  var shape = Polygon(List(
    Vec(width / 2, length / 2),
    Vec(0, length),
    Vec(-width / 2, length / 2),
    Vec(-width / 2, -length / 2),
    Vec(0, -length * 0.6),
    Vec(width / 2, -length / 2)
  ))


  getToNextSize(1)
  var du = Vec(0, 0)
  var u = Vec(1, 0)

  def isDead = health._1 <= 0 || health._2 <= 0

  def getShape = shape.rotate(-v, u).offset(position)

  def collisionCannonBall(cannonball: Cannonball):  Option[Boat => Unit] = {
    if (id != cannonball.boatId && (position - cannonball.position).length() < length) {
      getShape.collision2(cannonball.getShape) match {
        case Some(absolutePositionRo) => {
          if ((absolutePositionRo - position).dotProd(v) > 0)
            Option( boat =>  boat.health = (boat.health._1, math.max(0,boat.health._2 - 1)))
          else
            Option( boat =>  boat.health = (math.max(0, boat.health._1 - 1), boat.health._2))
        }
        case None => None
      }
    } else None
  }

  def position = Vec(x, y)

  def v = Vec(-u.y, u.x)

  def getToNextSize(newSize: Int) = {

    size = newSize
    shape = Polygon(List(
      Vec(width / 2, length / 2),
      Vec(0, length),
      Vec(-width / 2, length / 2),
      Vec(-width / 2, -length / 2),
      Vec(0, -length * 0.6),
      Vec(width / 2, -length / 2)
    ))

    health = (2 + size * 0.5, 2 + size * 0.5)
    val clients = clientId2position.keys

    clientId2position.empty
    position2moveTargets.empty
    position2shoots.empty
    position2firedelay.empty

    for (i <- 0 until size.toInt) {
      position2moveTargets += Position(i) -> None
      position2shoots += Position(i) -> false
      position2firedelay += Position(i) -> 0
    }

    clients.foreach(c => clientId2position += c -> nextFreePosition.get)

  }

  def nextFreePosition = {
    var lastFree = (0 until size.toInt).map(i => Position(i))
    clientId2position.values.foreach(positionExisting => lastFree = lastFree.filter(positionTested => positionTested != positionExisting))
    lastFree.headOption
  }

  def width: Double = length / 3.0

  def length: Double = 100.0 * math.sqrt(size)

  def setMoveTarget(id: String, pos: Vec) = {
    position2moveTargets(clientId2position(id)) = Option(pos)
  }

  def setShoot(id: String, b: Boolean) = {
    position2shoots(clientId2position(id)) = b
  }

  def addClient(newClient: String): Boolean = {
    val next = nextFreePosition
    if (next.isDefined) {
      clientId2position += newClient -> next.get
    }
    next.isDefined
  }

  def step(): List[Cannonball] = {
    val targets = collectTargets
    targets.size match {
      case 0 => {
        du *= 0.99
        speed *= 0.99
      }
      case numberOfTargets => {
        val positionSumOfTargets = targets.foldLeft(Vec(0, 0)) { (acc, n) => acc + n }
        val targetPosition = positionSumOfTargets / numberOfTargets
        val boatToTarget = targetPosition - position
        val unitDirectionToTarget =  boatToTarget.normalize()
        val oneOrDistanceLeft = math.min(1.0,math.max(0.0,boatToTarget.length-100.0) )
        val scalarIsEnough = math.pow (math.max(0, u.dotProd(unitDirectionToTarget) ),2)
        val newSpeed = (speed * 0.99 + u * 0.01 * 5  *oneOrDistanceLeft * scalarIsEnough  ) * 0.99
        du = (du * 0.92 + (unitDirectionToTarget - u) * 0.08  *oneOrDistanceLeft) * 0.99
        speed = newSpeed
      }
    }

    u = (u + du * 0.025).normalize()
    x += speed.x
    y += speed.y

    //Tire des boulets
    position2firedelay.keys.foreach(k => position2firedelay(k) = math.max(0, position2firedelay(k) - 1))
    var cannonBalls = List[Cannonball]()
    position2shoots.foreach(pair => {
      val position = pair._1
      val bool = pair._2
      if (bool && position2firedelay(position) <= 0) {
        position2firedelay(position) = 60
        val dx = (position.i % 2) * width - width / 2
        val n = (size+1)/2 -1
        val dy = ( n+1  - position.i/2)/(n+2)* length - length/2
        val dv = v * ((position.i % 2) - 0.5) * 2.0 * 5
        cannonBalls = new Cannonball( (math.sqrt(size)*80).toInt,   x + dx * v.x + dy * u.x, y + dx * v.y + dy * u.y, speed.x + dv.x, speed.y + dv.y, Random.alphanumeric.take(10).mkString, id) :: cannonBalls
      }
    })
    cannonBalls
  }

  def collectTargets = position2moveTargets.values.collect { case x: Some[Vec] => x.get }

  def angle = math.atan2(u.y, u.x)
}

class Cannonball(var lifeLeft : Int,var x: Double, var y: Double, var vx: Double, var vy: Double, val id: String, val boatId: String) extends Element {
  private val shape = Polygon(List(Vec(0, 4), Vec(4, 0), Vec(0, -4), Vec(-4, 0)))

  def getShape = shape.offset(position)

  def position = Vec(x, y)
  def step : Boolean = {
    lifeLeft-= 1
    lifeLeft <= 0
  }

}

case class Position(i: Int) {

}


class Obstacle(var x: Double, var y : Double, var id :String) extends Element {

  val numberOfVertex = 10
  val averageSize = 50-Random.nextInt(40)
  val fuzz = averageSize/2
 private val vertex = (0 until numberOfVertex).toList map {k => {
    val angle = math.Pi*2*k/ numberOfVertex
    Vec(Random.nextInt(fuzz)-fuzz/2 +  math.cos(angle) *averageSize,Random.nextInt(fuzz)-fuzz/2 + math.sin(angle)*averageSize)
  }}

  val shape = Polygon(vertex).offset(Vec(x,y))

}

case class Polygon(vertex: List[Vec]) {

 // println("new Polygon : \n" + vertex.map(_.toString).mkString("\n")  )
  val lastSegment = (vertex.last, vertex.head)
  val vertexSuccessivePair = lastSegment :: vertex.sliding(2).map(list => (list(0), list(1))).toList

  val normals = vertexSuccessivePair.map { case (a, b) => {
    val u = b - a
    Vec(-u.y, u.x)
  }
  }

  def rotate(u: Vec, v: Vec): Polygon = {
    Polygon(vertex.map(vertex => {

      u * vertex.x + v * vertex.y

    }))
  }

  def collision2(other: Polygon): Option[Vec] = {
    val vertexInsideMe = other.vertex.filter(ov => {
      val myOffsetedVertex = offset(-ov)
      var counter = 0.0
      myOffsetedVertex.vertexSuccessivePair.foreach { case (u, v) => {
        val w = u * v
        if (w.x < 0 && w.y > 0) {
          val mp = math.signum(u.y * v.x)
          counter += mp
        }
        else if (w.x > 0 && w.y < 0) {
          val pm = math.signum(-u.x * v.y)
          counter += pm
        }
        else if (w.x < 0 && w.y < 0) {
          val a = u
          val b = Vec(-a.y, a.x)
          val scalar = v.dotProd(b)
          val mm = -math.signum(scalar) * 2
          counter += mm
        }
      }
      }
      math.abs(counter) == 4
    })

    vertexInsideMe.size match {
      case 0 => {
        None
      }
      case s => {
        val sum = vertexInsideMe.foldLeft(Vec(0, 0)) {
          _ + _
        }
        Option(sum / s)
      }
    }
  }

  def offset(offset: Vec): Polygon = {
    Polygon(vertex.map(v => v + offset))
  }

  def collision(other: Polygon) = {
    val biNormals = normals ++ other.normals
    biNormals.forall(normal => {
      def getMinMax(onesVertex: List[Vec]) = {
        val projectedVertex = onesVertex.map(v => v.dotProd(normal))
        val min = projectedVertex.min
        val max = projectedVertex.max
        (min, max)
      }

      val myOwnMinMax = getMinMax(vertex)
      val otherMinMax = getMinMax(other.vertex)
      (myOwnMinMax._1 > otherMinMax._1 && myOwnMinMax._1 < otherMinMax._2) ||
        (myOwnMinMax._2 > otherMinMax._1 && myOwnMinMax._2 < otherMinMax._2)
    })
  }
}