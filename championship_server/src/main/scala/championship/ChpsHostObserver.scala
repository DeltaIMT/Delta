package championship

import akka.actor.FSM.->
import core.host.{HostObserver, HostPool}

class ChpsHostObserver extends HostObserver {
  val HP = HostPool[ChpsHost, ChpsHostObserver]
  var threshold = 0
  var pairingMap = collection.mutable.HashMap[String, String]()
  var notPairedYet = collection.mutable.HashMap[String, Int]()
  var boats = collection.mutable.HashMap[String, Boat]()

  override def clientInput(id: String, data: String): Unit = {}


  def getPositionFromId(id: String) = {
    boats(id).position
  }

  def deadBoat(id: String): Unit = {
    if (pairingMap.contains(id)) {
      val boatThatLostHisPair = pairingMap(id)
      HP.getHost(getPositionFromId(boatThatLostHisPair)).call( _.lostPair(boatThatLostHisPair))
      notPairedYet += boatThatLostHisPair -> boats(boatThatLostHisPair).size.toInt
      pairingMap -= pairingMap(id)
      pairingMap -= id
    }
    if (notPairedYet.contains(id)) {
      notPairedYet -= id
    }
    if (boats.contains(id)) {
      boats -= id
    }
  }

  def tick = {
    notPairedYet.foreach {
      case (id_A, size_A) => {
        val pairableOpt = notPairedYet.find {
          case (id_B: String, size_B: Int) => {
            id_A != id_B && math.abs(size_A - size_B) <= threshold
          }
          case _ => false
        }
        pairableOpt match {
          case Some((id_B, size_B)) => {
            pairingMap += id_A -> id_B
            pairingMap += id_B -> id_A
            notPairedYet -= id_A
            notPairedYet -= id_B
          }
          case None =>
        }
      }
    }
    pairingMap.foreach {
      case (id_A, id_B) => {
        if(boats.contains(id_A) && boats.contains(id_B) ){
          val updatedTarget = boats(id_B)

          HP.getHost(getPositionFromId(id_A)).call(host => {
            host.updateTarget(updatedTarget,id_A )
          })
        }

      }
    }
  }
}
