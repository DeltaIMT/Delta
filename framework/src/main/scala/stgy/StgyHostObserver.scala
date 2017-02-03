package stgy

import akka.actor.FSM.->
import core.host.{HostObserver, HostPool}
import play.api.libs.json.Json

import scala.util.Random

class StgyHostObserver extends HostObserver[StgyClientView] {

  var clientId2Color = collection.mutable.HashMap[String, Array[Int]]()
  var clientId2Aggregator = collection.mutable.HashMap[String, Aggregator]()
  var clientId2Xp = collection.mutable.HashMap[String, Double]()

  def gainxp(clientId: String, xp: Double) = {
    if (clientId2Xp.contains(clientId))
      clientId2Xp(clientId) += xp
    else
      clientId2Xp += clientId -> xp
  }


  def aggreg(clientId: String, objId: String, position: Vec) = {
    val aggregator = clientId2Aggregator.getOrElseUpdate(clientId, new Aggregator)
    aggregator.add(objId, position)
  }

  def deleteAggreg(clientId: String, objId: String) = {
    val aggregator = clientId2Aggregator.getOrElseUpdate(clientId, new Aggregator)
    aggregator.delete(objId)
  }


  def tick = {
    id2ClientView.foreach { case (id, cv) => {
      if (clientId2Xp.contains(id)) {
        cv.call(c => c.xp = clientId2Xp(id))
      }

      clientId2Aggregator get id match {
        case Some(c2a) => {
          c2a.update
          cv.call(c => {
            c.pos = c2a.get
            c.numberOfUnit = c2a.size
          })
        }
        case None => {}
      }

    }
    }
  }

  override def clientInput(clientId: String, data: String): Unit = {


    val json = Json.parse(data)
    val id = (json \ "id").get.as[String]
    if ((id == "1" || id == "2" || id == "3" || id == "8") &&
      clientId2Aggregator.contains(clientId) && clientId2Xp.contains(clientId)) {
      val ag = clientId2Aggregator(clientId)
      val pos = ag.get
      val idObj = Random.alphanumeric.take(10).mkString


      if (id == "1" && clientId2Xp(clientId) >= 1) {
        clientId2Xp(clientId) -= 1
        val unit = new Bowman(pos.x + Random.nextInt(200) - 100, pos.y + Random.nextInt(200) - 100, idObj, clientId, clientId2Color(clientId))
        val responsibleHost = HostPool[StgyHost, StgyHostObserver].getHost(pos)
        responsibleHost.call {_.elements += idObj -> unit   }
      }

      if (id == "2" && clientId2Xp(clientId) >= 3) {
        clientId2Xp(clientId) -= 3
        val unit = new Swordman(pos.x + Random.nextInt(200) - 100, pos.y + Random.nextInt(200) - 100, idObj, clientId, clientId2Color(clientId))
        val responsibleHost = HostPool[StgyHost, StgyHostObserver].getHost(pos)
        responsibleHost.call {_.elements += idObj -> unit   }
      }

      if (id == "3" && clientId2Xp(clientId) >= 10) {
        clientId2Xp(clientId) -= 10
        val unit = new Commander(pos.x + Random.nextInt(200) - 100, pos.y + Random.nextInt(200) - 100, idObj, clientId, clientId2Color(clientId))
        val responsibleHost = HostPool[StgyHost, StgyHostObserver].getHost(pos)
        responsibleHost.call {_.elements += idObj -> unit   }
      }

      if (id == "8"){
        clientId2Xp(clientId) += 1000
      }


    }


  }
}