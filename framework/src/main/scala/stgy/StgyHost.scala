package stgy

import akka.actor.ActorRef
import core.{HostPool, HyperHost}
import core.`abstract`.AbstractHost
import core.user_import.{Element, Zone}
import play.api.libs.json.Json

import scala.util.Random

class StgyHost(hostPool: HostPool, val zone: Zone) extends AbstractHost(hostPool) {
  var rand = new Random()
  var targetFromOtherHost = collection.mutable.HashMap[ActorRef, collection.mutable.HashMap[String, Unity]]()

  methods += "flush" -> ((arg: Any) => {
    elements = collection.mutable.HashMap[String, Element]()
  })

  methods += "addUnity" -> ((arg: Any) => {
    var e = arg.asInstanceOf[Unity]
    elements += e.id -> e
  })

  methods += "gainxp" -> ((arg: Any) => {
    var e = arg.asInstanceOf[String]
    if(elements.contains(e))
      elements(e).asInstanceOf[Evolving].gainKillXp
  })

  methods += "receiveTarget" -> ((arg: Any) => {
    var seq = arg.asInstanceOf[Seq[Any]]
    var who = seq(0).asInstanceOf[ActorRef]
    var e = seq(1).asInstanceOf[Iterable[Unity]]
    if (!targetFromOtherHost.contains(who))
      targetFromOtherHost += who -> collection.mutable.HashMap[String, Unity]()

    targetFromOtherHost(who).clear()
    e.foreach(b => {
      targetFromOtherHost(who)(b.id) = b
    })

  })

  var neighbours = List[HyperHost]()
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

  override def tick(): Unit = {

    getNeighbours

    val unitys =  elements.filter(e => e._2.isInstanceOf[Unity]).values.asInstanceOf[Iterable[Unity]]

    val damagable =  elements.filter(e => e._2.isInstanceOf[Damagable]).values.asInstanceOf[Iterable[Damagable]]
    val bowmen= damagable.filter( x => x.isInstanceOf[Bowman]).asInstanceOf[Iterable[Bowman]]
    val coms = damagable.filter( x => x.isInstanceOf[Commander]).asInstanceOf[Iterable[Commander]]
    var otherDamagable = List[Damagable]()
    targetFromOtherHost.values.map(x => x.values).foreach(x =>  otherDamagable ++= x.asInstanceOf[Iterable[Damagable]])
    val otherBowmen = otherDamagable.filter(x => x.isInstanceOf[Bowman]).asInstanceOf[Iterable[Bowman]]
    val extendedDamagable = damagable ++ otherDamagable
    val flags = elements.filter(e => e._2.isInstanceOf[Flag]).values.asInstanceOf[Iterable[Flag]]
    val arrows = elements.filter(e => e._2.isInstanceOf[Arrow]).values.asInstanceOf[Iterable[Arrow]]
    val spawner = elements.filter(e => e._2.isInstanceOf[Spawner]).values.asInstanceOf[Iterable[Spawner]]
    neighbours.foreach(h => h.method("receiveTarget", Seq(self, damagable)))

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

            if(elements.contains(a.shooterId))
              elements(a.shooterId).asInstanceOf[Evolving].gainKillXp
            else{
              neighbours.foreach(  h => h .method("gainxp" , a.shooterId))
            }

          }
        })
      }
    }

    spawner.foreach( u => {
      u.spawnerStep
      if(u.canSpawn){
        val spawned = u.spawn
        u.clientViews.foreach(cv => spawned.sub(cv))
        elements += spawned.id -> spawned
      }
    })
    damagable.foreach(u => if (u.isDead) elements -=u.id )

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
      A.notifyClientViews
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
        val targets = 1 to 10 map { i => target + Vec(Random.nextInt(50)-25,Random.nextInt(50)-25) }
        val arrows = A.shoot(targets.toList)
        arrows.foreach( arrow => elements += arrow.id -> arrow)
      }
      A.notifyClientViews
    }
    }

    elements foreach { elem => {
      val e = elem._2
      if (!zone.contains(e)) {
        //     println("Il faut sortir de " + zone.x + " " + zone.y)
        hostPool.getHyperHost(e.x, e.y).method("addUnity", e)
        elements -= elem._1
      }
    }
    }


  }

  override def clientInput(id: String, data: String): Unit = {

    //  println("DATA RECEIVED : " + data)
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
