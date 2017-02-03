package stgy

import akka.actor.FSM.->
import core.observerPattern.{Observable, Observer}

import scala.util.Random



trait Unity extends Element with Observable {
  var id: String
  var clientId: String
  var color: Array[Int]
}

trait Movable extends Unity {
  var move = false
  var target = Vec()
  var speed : Double

  def getSpeed : Vec = {
    val pos = Vec(x, y)
    val dir = (target - pos).normalize()
    dir * speed * (if (move) 1.0 else 0.0)
  }

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
  var maxHealth = 1.0
  var radius : Int
  var xpCost : Double

  def damage(amount: Double) = {
    health = math.max(0, health - amount)
  }

  def damagableStep = {
    health = math.min(maxHealth, health + 0.002*maxHealth)
  }

  def isDead: Boolean = health <= 0
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

trait Evolving{
  var xp =0.0
  var gainPerKill = 1.0
  def gainKillXp = {xp += gainPerKill}
}

trait Spawner extends Unity{
  var frameToSpawn = 500
  var canSpawnIn = 0
  def spawnerStep = {
    if (canSpawnIn>0)
      canSpawnIn -= 1
  }

  def canSpawn: Boolean = {
    false//canSpawnIn == 0
  }

  def spawn: Unity = {
    canSpawnIn = frameToSpawn
    if (Random.nextBoolean() )  new Bowman(x, y, Random.alphanumeric.take(10).mkString, clientId, color) else new Swordman(x, y, Random.alphanumeric.take(10).mkString, clientId, color)
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

class Commander(var x : Double,var y : Double,var id : String,var clientId : String,var color : Array[Int]) extends Movable with Damagable with Shooter with Spawner with Evolving{
  health = 2
  maxHealth = 2
  var speed = 1.0
  var xpCost = 10.0
  override var radius: Int =  25


  def shoot(target: Unity): List[Arrow] = {
    target match {
      case e:Movable => {
        shoot(Vec(e.x, e.y) + e.getSpeed *30)
      }
      case e:Damagable => {
        shoot(Vec(e.x,e.y))
      }
    }
  }

  def shoot(target: Vec): List[Arrow] = {
    canShootIn = 60 / (xp.toInt+1) + 20
    val targets = 1 to 10 map { i => target + Vec(Random.nextInt(50) - 25, Random.nextInt(50) - 25) }
    val arrows =targets.map( t => {
     val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color, id)
      arrow.move=true
      val direction= (t-Vec(x,y)).normalize()
      arrow.target = Vec(x,y)+ (direction*1000.0)
      arrow.speed = arrow.speed - 5 + Random.nextInt(10)
      arrow
    })
    arrows.toList
  }

  def step() = {
    doMove
    shooterStep
    damagableStep
  }


}

class Aggregator  {

  var obj2position = collection.mutable.HashMap[String, Vec]()
  var cameraPos = Vec(0,0)
  var cameraVel = Vec(0,0)

  def size = obj2position.size
  def get = {
    cameraPos
  }

  def update = {
    val perfectPos = obj2position.values.foldLeft(Vec(0,0)){_+_ }/ (if(size>0) size.toDouble else 1.0)
    cameraVel = (perfectPos- cameraPos)*0.2
    cameraPos = cameraPos + cameraVel
  }

  def add(objId : String, v: Vec) = {
    obj2position += objId -> v
  }

  def delete(objId : String) = obj2position-= objId

}


class Swordman(var x : Double,var y : Double,var id : String,var clientId : String,var color : Array[Int]) extends Movable with Damagable with Shooter with Evolving {
  override var radius: Int = 20
  maxHealth = 3
  health = 3
  var speed = 2.0
  var xpCost = 3.0
  var damage = 5*0.201/6.0
  def step() = {
    doMove
    shooterStep
    damagableStep
  }

  def canAttack(e : Damagable) : Boolean = {
    val vector = Vec(x,y)-Vec(e.x,e.y)
    vector.length()<50
  }

  def attack(e : Damagable) : Double = {
    canShootIn = 10
    damage
  }

}

class Bowman(var x : Double,var y : Double,var id : String,var clientId : String,var color : Array[Int]) extends Movable with Damagable with Shooter with Evolving {
  var speed = 1.0
  var xpCost = 1.0
  override var radius: Int =  20

  def shoot(target: Unity): Arrow = {
    target match {
      case e:Movable => {
        shoot(Vec(e.x, e.y) + e.getSpeed *30)
      }
      case e:Damagable => {
        shoot(Vec(e.x,e.y))
      }
    }
  }

  def shoot(target: Vec): Arrow = {
    canShootIn = 60 /(xp.toInt+1) +20
    val arrow = new Arrow(x, y, Random.alphanumeric.take(10).mkString, clientId, color , id)
    arrow.move = true
    val direction= (target-Vec(x,y)).normalize()
    arrow.target = Vec(x,y)+ (direction*1000.0)
    arrow
  }

  def step() = {
    doMove
    shooterStep
    damagableStep
  }
}

class Arrow(var x : Double,var y : Double,var id : String,var clientId : String,var color :Array[Int], var shooterId : String) extends Movable {
  var speed = 10.0
  var frame = 0
  var shotFrom = Vec(x,y)
  def shouldDie: Boolean = {
    frame+=1
    frame==60
  }
}
