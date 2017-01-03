package stgy

import akka.actor.ActorRef
import core.{HostPool, HyperHost}
import core.`abstract`.AbstractHost
import core.user_import.Zone
import play.api.libs.json.Json

import scala.util.Random

class StgyHost(hostPool: HostPool, val zone: Zone) extends AbstractHost(hostPool) {
  var rand = new Random()
  var targetFromOtherHost = collection.mutable.HashMap[ ActorRef,collection.mutable.HashMap[String, Bowman]]()

  methods += "addUnity" -> ((arg: Any) => {
    var e = arg.asInstanceOf[Unity]
    elements += e.id -> e
  })

  methods += "receiveTarget" -> (  (arg:Any)  =>{
    var seq= arg.asInstanceOf[Seq[Any]]
    var who = seq(0).asInstanceOf[ActorRef]
    var e = seq(1).asInstanceOf[Iterable[Bowman]]
    if(!targetFromOtherHost.contains(who))
      targetFromOtherHost += who -> collection.mutable.HashMap[String, Bowman]()

    targetFromOtherHost(who).clear()
    e.foreach( b => {
      targetFromOtherHost(who)(b.id) = b
    })

  })

  var neighbours = List[HyperHost]()


  override def tick(): Unit = {

    if(neighbours.isEmpty)
    {
      if(zone.x < hostPool.wn*hostPool.w){
        neighbours::=   hostPool.getHyperHost(zone.x + zone.w, zone.y)
        if(zone.y < hostPool.hn*hostPool.h)
          neighbours::=   hostPool.getHyperHost(zone.x + zone.w , zone.y + zone.h)
        if(zone.y >= hostPool.h)
          neighbours::=   hostPool.getHyperHost(zone.x + zone.w , zone.y - zone.h)
      }
      if(zone.x >= hostPool.w){
        neighbours::=   hostPool.getHyperHost(zone.x - zone.w, zone.y)
        if(zone.y < hostPool.hn*hostPool.h)
          neighbours::=   hostPool.getHyperHost(zone.x - zone.w, zone.y + zone.h)
        if(zone.y >= hostPool.h)
          neighbours::=   hostPool.getHyperHost(zone.x - zone.w, zone.y - zone.h)
      }
      if(zone.y < hostPool.hn*hostPool.h)
        neighbours::=   hostPool.getHyperHost(zone.x , zone.y + zone.h)
      if(zone.y >= hostPool.h)
        neighbours::=   hostPool.getHyperHost(zone.x , zone.y - zone.h)
    }


    val bowmen = elements.filter(e => e._2.isInstanceOf[Bowman]).values.asInstanceOf[Iterable[Bowman]]
    neighbours.foreach( h => h.method("receiveTarget" , Seq(self,bowmen) ))


    var otherBowmen = List[Bowman]()
    targetFromOtherHost.values.map(x => x.values).foreach( x=> otherBowmen ++= x)

    val extendedBowmen = bowmen ++ otherBowmen

    val flags = elements.filter(e => e._2.isInstanceOf[Flag]).values.asInstanceOf[Iterable[Flag]]
    val arrows = elements.filter(e => e._2.isInstanceOf[Arrow]).values.asInstanceOf[Iterable[Arrow]]

    flags foreach { f =>{
      f.step
      f.computePossessing(extendedBowmen)
      if(f.canSpawn) {
        val spawned = f.spawn
        f.clientViews.foreach( cv => spawned.sub(cv))
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
        val enemy = bowmen filter { b => b.clientId != a.clientId }
        enemy.foreach(e => {
          if ((Vec(e.x, e.y) - Vec(a.x, a.y)).length < 20) {
            e.damage(0.201)
            elements -= a.id
          }
        })
      }
    }

    bowmen foreach { A => {
      A.step
      if (A.isDead)
        elements -= A.id
      var closest: (Double, Bowman) = (Double.MaxValue, null)
      //bowmen.foreach(B => {
        extendedBowmen.foreach(B => {
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
      val bm = elements(id).asInstanceOf[Bowman]
      bm.move = true
      bm.target = Vec(x, y)
    }
  }
}
