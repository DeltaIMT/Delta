package stgy

import core.host.{HostObserver, HostPool}
import play.api.libs.json.Json
import stgy.StgyTypes.UnitType.UnitType
import stgy.StgyTypes._

import scala.util.Random

class StgyHostObserver extends HostObserver[StgyClientView] {

  var clientId2Color = collection.mutable.Map[ClientId, Color]()
  var clientId2Aggregator = collection.mutable.Map[ClientId, Aggregator]()
  var clientId2Xp = collection.mutable.Map[ClientId, Double]()
  var clientId2ListObjId = collection.mutable.Map[ClientId, List[UnitId] ]()
  var objId2pos = collection.mutable.Map[UnitId, Vec]()
  var objId2Color = collection.mutable.Map[UnitId, Color]()
  var objId2UnitType= collection.mutable.Map[UnitId, UnitType]()

  def gainxp(clientId: ClientId, xp: Double) = {
    if (clientId2Xp.contains(clientId))
      clientId2Xp(clientId) += xp
    else
      clientId2Xp += clientId -> xp
  }



  def aggreg(clientId2Info : Map[ClientId,List[(UnitId,Vec)]]): Unit = {
    clientId2Info.foreach {case (clientId,list) => {
      list.foreach {case (objId, position) => aggreg(clientId,objId,position)}
    }}
  }


  def aggreg(clientId: ClientId, objId: UnitId, position: Vec) : Unit = {
    val aggregator = clientId2Aggregator.getOrElseUpdate(clientId, new Aggregator)
    aggregator.add(objId, position)
  }

  def deleteAggreg(clientId: ClientId, objId: UnitId) = {
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

  override def clientInput(clientId: ClientId, data: String): Unit = {


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