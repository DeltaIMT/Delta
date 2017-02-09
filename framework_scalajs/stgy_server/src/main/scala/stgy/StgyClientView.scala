package stgy

import java.nio.ByteBuffer

import akka.actor.ActorRef
import akka.actor.FSM.->
import core.clientView.{ClientView, ClientViewActor}
import core.host.{HostObserver, HostPool}
import core.spatial.Zone
import boopickle.Default._


class StgyClientView(id :String) extends ClientView(id) {

  val HP = HostPool[StgyHost, StgyHostObserver]
  var pos = Vec(1500, 1500)
  var xp = 0.0
  var numberOfUnit = 1.0

  var hashIdColor = collection.mutable.Map[String, Boolean]()
  var hashIdChangeHost = collection.mutable.Map[String, Boolean]()
  var hashTime = collection.mutable.Map[String, Int]()

  override def dataToViewZone(): Zone = new SquareZone(pos.x - 1920/2, pos.y - 1080/2, 1920, 1080)

  override def onNotify(any: Any): Unit = {

    any match {
      case _ => {
       // println("notify not matched")
      }
    }
  }

  override def onDisconnect(any: Any): Unit = {
    //  hostPool.getHyperHost(x, y).exec(l => l -= idBall)
  }

  override def fromListToClientMsg(list: List[Any]) = {

    // hashTime uploaded each time "fromListToClientMsg is called :
    // if the element is unknown, we added it inside hashTime, else we bring its value to 5
    val unitys = list.filter(x => x.isInstanceOf[Unity]).asInstanceOf[List[Unity]]
    unitys.foreach(u => {
      if (!hashIdColor.contains(u.id))
        hashIdColor += u.id -> false
      else
        hashIdColor(u.id) = true
      // hashTime uploaded each time "fromListToClientMsg is called :
      // if the element is unknown, we added it inside hashTime, else we bring its value to 5
      if (!hashTime.contains(u.id))
        hashTime += u.id -> 5
      else {
        if (!hashIdChangeHost.contains(u.id)) {
          hashIdChangeHost += u.id -> true
        }
        else {
          if (hashTime(u.id) == 4)
            hashIdChangeHost(u.id) = false
          else
            hashIdChangeHost(u.id) = true
        }
        hashTime(u.id) = 5
      }
    }
    )
    // while processing "fromListToClientMsg", we decrease the value of each key we have
    hashTime.keys.foreach(k => hashTime(k) = hashTime(k) - 1)

    val arrows = list collect { case a:Arrow => ArrowFront(a.id,a.x.toInt,a.y.toInt) }




    val damagable =  list.collect {case a: Damagable => a }
    val meta = damagable. collect { case a: MetaUnit => a}
    val units = meta map {a => UnityFront(a.id,a.color, a.x.toInt,a.y.toInt , (a.health/a.maxHealth).toFloat )}

    val frame = StgyFrame(units,arrows , pos.x.toFloat, pos.y.toFloat, xp.toFloat, numberOfUnit.toInt)


    val buf = Pickle.intoBytes(frame)
    Right(buf)
  }
}
