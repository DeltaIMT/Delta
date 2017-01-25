package stgy

import akka.actor.ActorRef
import core.user_import.{Element, Zone}
import core.{Host, HostPool, HyperHost}
import kamon.Kamon
import play.api.libs.json.Json
import scala.util.Random

class StgyHost(hostPool: HostPool[StgyHost], zone: Zone) extends Host(hostPool, zone) {
  val trace = true
  val counter = Kamon.metrics.counter(getName("counter"))
  var rand = new Random()

  var targetFromOtherHost = collection.mutable.HashMap[ActorRef, collection.mutable.HashMap[String, Unity]]()
  var neighbours = List[HyperHost[StgyHost]]()

  var aggregs = collection.mutable.HashMap[String, Aggregator]()

  def getName(name: String) = "host-" + name + getNum

  def flush() = {
    elements = collection.mutable.HashMap[String, Element]()
  }

  override def tick(): Unit = {
    getNeighbours
    counter.increment()
    val unitys = elements.filter(e => e._2.isInstanceOf[Unity]).values.asInstanceOf[Iterable[Unity]]

    val damagable = elements.filter(e => e._2.isInstanceOf[Damagable]).values.asInstanceOf[Iterable[Damagable]]
    val bowmen = damagable.filter(x => x.isInstanceOf[Bowman]).asInstanceOf[Iterable[Bowman]]
    val coms = damagable.filter(x => x.isInstanceOf[Commander]).asInstanceOf[Iterable[Commander]]
    var otherDamagable = List[Damagable]()
    targetFromOtherHost.values.map(x => x.values).foreach(x => otherDamagable ++= x.asInstanceOf[Iterable[Damagable]])
    val otherBowmen = otherDamagable.filter(x => x.isInstanceOf[Bowman]).asInstanceOf[Iterable[Bowman]]
    val extendedDamagable = damagable ++ otherDamagable
    val flags = elements.filter(e => e._2.isInstanceOf[Flag]).values.asInstanceOf[Iterable[Flag]]
    val arrows = elements.filter(e => e._2.isInstanceOf[Arrow]).values.asInstanceOf[Iterable[Arrow]]
    val spawner = elements.filter(e => e._2.isInstanceOf[Spawner]).values.asInstanceOf[Iterable[Spawner]]
    neighbours.foreach(h => if (trace) h.callTrace(_.receiveTarget(self, damagable), "receiveTarget") else h.call(_.receiveTarget(self, damagable)))


    flags foreach { f => {
      f.step
      f.computePossessing(extendedDamagable)
      if (f.canSpawn) {
        val spawned = f.spawn
        f.clientViews.foreach(cv => spawned.sub(cv))
        elements += spawned.id -> spawned
      }
    }
    }

    arrows foreach {
      a => {
        if (a.shouldDie) {
          elements -= a.id
        }
        else
          a.doMove
        val enemy = damagable filter { b => b.clientId != a.clientId }
        enemy.foreach(e => {
          if ((Vec(e.x, e.y) - Vec(a.x, a.y)).length < e.radius) {
            e.damage(0.201)
            elements -= a.id

            if (elements.contains(a.shooterId))
              elements(a.shooterId).asInstanceOf[Evolving].gainKillXp
            else {
              neighbours.foreach(h => if (trace) h.callTrace(_.gainxp(a.shooterId), "gainxp") else h.call(_.gainxp(a.shooterId)))
            }

          }
        })
      }
    }

    spawner.foreach(u => {
      u.spawnerStep
      if (u.canSpawn) {
        val spawned = u.spawn
        u.clientViews.foreach(cv => spawned.sub(cv))
        elements += spawned.id -> spawned
      }
    })
    damagable.foreach(u => if (u.isDead) elements -= u.id)

    bowmen foreach { A => {
      A.step
      var closest: (Double, Damagable) = (Double.MaxValue, null)
      //bowmen.foreach(B => {
      extendedDamagable.foreach(B => {
        if (B.clientId != A.clientId) {
          val distance = (Vec(A.x, A.y) - Vec(B.x, B.y)).length()
          if (distance < closest._1)
            closest = (distance, B)
        }
      })
      if (A.canShoot && closest._1 < 350) {
        val arrow = A.shoot(Vec(closest._2.x, closest._2.y))
        elements += arrow.id -> arrow
      }
   //   A.notifyClientViews
    }
    }

    coms foreach { A => {
      A.step
      var closest: (Double, Damagable) = (Double.MaxValue, null)
      //bowmen.foreach(B => {
      extendedDamagable.foreach(B => {
        if (B.clientId != A.clientId) {
          val distance = (Vec(A.x, A.y) - Vec(B.x, B.y)).length()
          if (distance < closest._1)
            closest = (distance, B)
        }
      })
      if (A.canShoot && closest._1 < 350) {
        val target = Vec(closest._2.x, closest._2.y)
        val targets = 1 to 10 map { i => target + Vec(Random.nextInt(50) - 25, Random.nextInt(50) - 25) }
        val arrows = A.shoot(targets.toList)
        arrows.foreach(arrow => elements += arrow.id -> arrow)
      }
    //  A.notifyClientViews
    }
    }

    elements foreach { elem => {
      val e = elem._2
      if (!zone.contains(e)) {
        //     println("Il faut sortir de " + zone.x + " " + zone.y)
        if (trace)
          hostPool.getHyperHost(e.x, e.y).callTrace(_.addUnity(e.asInstanceOf[Unity]), "addUnity")
        else
          hostPool.getHyperHost(e.x, e.y).call(_.addUnity(e.asInstanceOf[Unity]))
        elements -= elem._1
      }
    }
    }



    aggregs.values.foreach( a => {
      a.minXY = Vec(3000,3000)
      a.maxXY = Vec(0,0)
    })


    var listId = List[String]()

    unitys.filter( u => !u.isInstanceOf[Arrow]) .foreach( u => {

      listId ::= u.clientId
      if (!aggregs.contains(u.clientId) ) {
        aggregs += u.clientId -> new Aggregator(u.clientId, u.x, u.y)
        if(u.clientViews.size >0)
        aggregs(u.clientId).sub(u.clientViews.head)
      }
      else {
        aggregs(u.clientId).minXY = Vec( math.min(aggregs(u.clientId).minXY.x, u.x),  math.min(aggregs(u.clientId).minXY.y, u.y)  )
        aggregs(u.clientId).maxXY = Vec( math.max(aggregs(u.clientId).maxXY.x, u.x),  math.max(aggregs(u.clientId).maxXY.y, u.y)  )
      }
    })

    listId = listId.distinct
    aggregs.values.foreach( a => {
      if (!listId.contains(a.clientId))
        aggregs -= a.clientId
      else
        a.notifyClientViews
    })



  }

  def addUnity(e: Unity) = {
    elements += e.id -> e
  }

  def gainxp(e: String) = {
    if (elements.contains(e))
      elements(e).asInstanceOf[Evolving].gainKillXp
  }

  def receiveTarget(who: ActorRef, e: Iterable[Unity]) = {
    if (!targetFromOtherHost.contains(who))
      targetFromOtherHost += who -> collection.mutable.HashMap[String, Unity]()

    targetFromOtherHost(who).clear()
    e.foreach(b => {
      targetFromOtherHost(who)(b.id) = b
    })
  }

  def getNeighbours = {
    if (neighbours.isEmpty) {
      if (zone.x < hostPool.wn * hostPool.w) {
        neighbours ::= hostPool.getHyperHost(zone.x + zone.w, zone.y)
        if (zone.y < hostPool.hn * hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x + zone.w, zone.y + zone.h)
        if (zone.y >= hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x + zone.w, zone.y - zone.h)
      }
      if (zone.x >= hostPool.w) {
        neighbours ::= hostPool.getHyperHost(zone.x - zone.w, zone.y)
        if (zone.y < hostPool.hn * hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x - zone.w, zone.y + zone.h)
        if (zone.y >= hostPool.h)
          neighbours ::= hostPool.getHyperHost(zone.x - zone.w, zone.y - zone.h)
      }
      if (zone.y < hostPool.hn * hostPool.h)
        neighbours ::= hostPool.getHyperHost(zone.x, zone.y + zone.h)
      if (zone.y >= hostPool.h)
        neighbours ::= hostPool.getHyperHost(zone.x, zone.y - zone.h)
    }
  }

  override def clientInput(id: String, data: String): Unit = {

    val json = Json.parse(data)
    val id = (json \ "id").get.as[String]
    val x = (json \ "x").get.as[Double]
    val y = (json \ "y").get.as[Double]

    if (elements.contains(id)) {
      val bm = elements(id).asInstanceOf[Movable]
      bm.move = true
      bm.target = Vec(x, y)
    }
  }
}
