package stgy

import core.user_import.{Element, Observable, Observer}

import scala.util.Random



trait Unity extends Element with Observable {
  var id: String
  var clientId: String
  var color: Array[Int]
}

trait Movable extends Unity {
  var move = false
  var target = Vec()
  var speed = 5

  def doMove = {
    if ((Vec(x, y) - target).length() < 1)
      move = false
    if (move) {
      var toTarget = target - Vec(x, y)
      var length = toTarget.length()
      toTarget /= length
      x += toTarget.x * speed * math.min(length / speed, 1)
      y += toTarget.y * speed * math.min(length / speed, 1)
    }
  }
}

trait Damagable extends Unity {
  var health = 1.0
  var radius : Int

  def damage(amount: Double) = {
    health = math.max(0, health - amount)
  }

  def isDead: Boolean = health == 0
}

trait Shooter {
  var canShootIn = 0
  def canShoot = {
    canShootIn == 0
  }

  def shooterStep = {
    if (canShootIn > 0)
      canShootIn -= 1
  }
}

trait Spawner extends Unity{
  var frameToSpawn = 500
  var canSpawnIn = 0
  def spawnerStep = {
    if (canSpawnIn>0)
      canSpawnIn -= 1
  }

  def canSpawn: Boolean = {
    canSpawnIn == 0
  }

  def spawn: Unity = {
    canSpawnIn = frameToSpawn
    new Bowman(x, y, Random.alphanumeric.take(10).mkString, clientId, color)
  }
}

class Flag(var x : Double,var y : Double,var id : String,var clientId : String,var color : Array[Int]) extends Unity {

  var numberOfUnitToConvertInOneFrame = 500.0
  var frameToSpawn = 1300

  var isPossessed = true
  var possessing = 1.0

  var canSpawnIn = 0

  def computePossessing(units: Iterable[Unity]) = {

    val unitsInRange = units.filter(u => (Vec(u.x, u.y) - Vec(x, y)).length() < 200)

    val unitsAllyCount = unitsInRange.count(u => u.clientId == clientId)
    val unitsEnemyCount = unitsInRange.count(u => u.clientId != clientId)

    if( unitsAllyCount == 0)
      possessing  = math.max(0.0, possessing - (10) / numberOfUnitToConvertInOneFrame)

    if (unitsAllyCount < unitsEnemyCount)
      possessing = math.max(0.0, possessing - (unitsEnemyCount - unitsAllyCount) / numberOfUnitToConvertInOneFrame)
    else
      possessing = math.min(1.0, possessing + (unitsAllyCount - unitsEnemyCount) / numberOfUnitToConvertInOneFrame)

    if (possessing > 0.5)
      isPossessed = true
    else
      isPossessed = false

    if (possessing == 0 && unitsEnemyCount>0) {
      isPossessed = false
      clientViews = List[Observer]()
      //We need to find the team with the most unit
      var hash = collection.mutable.HashMap[String, (Int, Unity)]()

      unitsInRange.foreach(u => {
        if (hash.contains(u.clientId))
          hash(u.clientId) = (hash(u.clientId)._1 + 1, hash(u.clientId)._2)
        else
          hash += u.clientId -> (1, u)
      })
      val pair = hash.reduce((a, b) => if (a._2._1 > b._2._1) a else b)
      clientId = pair._1
      color = pair._2._2.color
      sub(pair._2._2.clientViews.head)
    }
  }

  def step = {
    if (canSpawnIn>0)
      canSpawnIn -= 1
  }

  def canSpawn: Boolean = {
    canSpawnIn == 0 && isPossessed
  }

  def spawn: Unity = {
    canSpawnIn = frameToSpawn
    new Bowman(x, y, Random.alphanumeric.take(10).mkString, clientId, color)
  }

}

class Commander(var x : Double,var y : Double,var id : String,var clientId : String,var color : Array[Int]) extends Movable with Damagable with Shooter with Spawner{
  health = 5
  override var radius: Int =  25
  def shoot(targets: List[Vec]): List[Arrow] = {
    canShootIn = 60
    val arrows =targets.map( t => {
     val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color)
      arrow.move=true
      val direction= (t-Vec(x,y)).normalize()
      arrow.target = Vec(x,y)+ (direction*1000.0)
      arrow.speed = arrow.speed - 5 + Random.nextInt(10)
      arrow
    })
    arrows
  }

  def step() = {
    doMove
    shooterStep
  }


}

class Bowman(var x : Double,var y : Double,var id : String,var clientId : String,var color : Array[Int]) extends Movable with Damagable with Shooter {

  override var radius: Int =  20
  def shoot(target: Vec): Arrow = {
    canShootIn = 60
    val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color)
    arrow.move = true
    val direction= (target-Vec(x,y)).normalize()
    arrow.target = Vec(x,y)+ (direction*1000.0)
    arrow
  }

  def step() = {
    doMove
    shooterStep
  }
}

class Arrow(var x : Double,var y : Double,var id : String,var clientId : String,var color :Array[Int]) extends Movable {
  speed = 10
  var frame = 0
  def shouldDie: Boolean = {
    frame+=1
    frame==60
  }
}
