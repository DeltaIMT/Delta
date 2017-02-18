package stgy

import akka.actor.FSM.->
import core.observerPattern.{Observable, Observer}
import stgy.StgyTypes.UnitType.UnitType
import stgy.StgyTypes.{ClientId, Color, UnitId, UnitType}

import scala.util.Random


trait MetaUnit{
  val metaType : UnitType
}

trait Unity extends Element with Observable {
  var id: UnitId
  var clientId: ClientId
  var color: Color
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

class Commander(var x : Double,var y : Double,var id : UnitId,var clientId : ClientId,var color : Color) extends Movable with Damagable with Shooter with Spawner with Evolving with MetaUnit{
  val metaType = UnitType .Com
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


class Swordman(var x : Double,var y : Double,var id : UnitId,var clientId : ClientId,var color : Color) extends Movable with Damagable with Shooter with Evolving with MetaUnit {
  val metaType = UnitType .Sword
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

class Bowman(var x : Double,var y : Double,var id : UnitId,var clientId : ClientId,var color : Color) extends Movable with Damagable with Shooter with Evolving with MetaUnit {
  val metaType = UnitType .Bow
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

class Arrow(var x : Double,var y : Double,var id : UnitId,var clientId : ClientId,var color :Color, var shooterId : UnitId) extends Movable {
  var speed = 10.0
  var frame = 0
  var shotFrom = Vec(x,y)
  def shouldDie: Boolean = {
    frame+=1
    frame==60
  }

  val id2 : ClientId = id

}
