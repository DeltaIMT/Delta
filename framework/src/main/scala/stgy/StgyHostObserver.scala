package stgy

import akka.actor.FSM.->
import core.host.{HostObserver, HostPool}
import play.api.libs.json.Json
import stgy.StgyTypes.UnitType.UnitType
import stgy.StgyTypes._

import scala.util.Random

class StgyHostObserver extends HostObserver[StgyClientView] {

  var clientId2Color = collection.mutable.Map[ClientId, Color]()
  var clientId2Xp = collection.mutable.Map[ClientId, Double]()
  var clientId2ListObjId = collection.mutable.Map[ClientId, Set[UnitId]]()
  var objId2Pos = collection.mutable.Map[UnitId, Vec]()
  var objId2ClientId = collection.mutable.Map[UnitId, ClientId]()
  var objId2UnitType= collection.mutable.Map[UnitId, UnitType]()


  def addUnity(unitId: UnitId, clientId: ClientId, position: Vec, unitType: UnitType) = {
    objId2Pos(unitId) = position
    objId2ClientId(unitId) = clientId
    objId2UnitType(unitId) = unitType
    if(clientId2ListObjId.contains(clientId))
    clientId2ListObjId(clientId) =  clientId2ListObjId(clientId)+unitId
    else
      clientId2ListObjId += clientId-> Set(unitId)
  }

  def deleteUnity(unitId: UnitId, clientId: ClientId) = {
    objId2Pos-= unitId
    objId2ClientId-= unitId
    objId2UnitType-= unitId
    if(clientId2ListObjId.contains(clientId))
    clientId2ListObjId(clientId) = clientId2ListObjId(clientId) - unitId
  }


  def updatePosition(id: UnitId, position : Vec) = {
    objId2Pos += id -> position
  }

  def addClient(clientId : ClientId, color : Color) = {
    clientId2Color += clientId -> color
    clientId2Xp += clientId -> 0
    if(!clientId2ListObjId.contains(clientId))
    clientId2ListObjId += clientId -> Set[UnitId]()

  }

  def deleteClient(clientId: ClientId)= {
    val objIds = clientId2ListObjId(clientId)
    clientId2Color-=clientId
    clientId2Xp-=clientId
    clientId2ListObjId-=clientId

    objIds.  foreach( oid => {
      objId2Pos  -= oid
      objId2ClientId -=oid
      objId2UnitType -= oid
    })
  }


  def gainxp(clientId: ClientId, xp: Double) = {
    if(clientId2Xp.contains(clientId))
      clientId2Xp(clientId) += xp/2.0
  }


  def getComPosition(clientId: ClientId) : Option[Vec] = {
    if(clientId2ListObjId.contains(clientId))
    clientId2ListObjId(clientId).find( id => objId2UnitType.contains(id) && objId2UnitType(id) ==UnitType.Com ) match {
      case Some(comId) => Option(objId2Pos(comId))
      case None =>None
    }
    else None
  }

  def tick = {
    id2ClientView.foreach { case (id, cv) => {
      if (clientId2Xp.contains(id) && clientId2Color.contains(id) && clientId2ListObjId.contains(id) ) {
        cv.call(c => {
          getComPosition(id) match  {
            case Some(v) =>  c.pos =v
            case None =>
          }
          c.numberOfUnit = clientId2ListObjId(id).size
          c.xp = clientId2Xp(id)
        })
      }
    }
    }
  }

  override def clientInput(clientId: ClientId, data: String): Unit = {

    val json = Json.parse(data)
    val id = (json \ "id").get.as[String]
    if ((id == "1" || id == "2" || id == "3" || id == "8") &&
      clientId2Xp.contains(clientId)) {
      getComPosition(clientId) match {
        case Some(pos) => {
          val idObj = Random.alphanumeric.take(10).mkString
          if (id == "1" && clientId2Xp(clientId) >= 1) {
            clientId2Xp(clientId) -= 1
            val unit = new Bowman(pos.x + Random.nextInt(200) - 100, pos.y + Random.nextInt(200) - 100, idObj, clientId, clientId2Color(clientId))
            val responsibleHost = HostPool[StgyHost, StgyHostObserver].getHost(pos)
            responsibleHost.call {_.addUnity(unit)   }
          }
          if (id == "2" && clientId2Xp(clientId) >= 3) {
            clientId2Xp(clientId) -= 3
            val unit = new Swordman(pos.x + Random.nextInt(200) - 100, pos.y + Random.nextInt(200) - 100, idObj, clientId, clientId2Color(clientId))
            val responsibleHost = HostPool[StgyHost, StgyHostObserver].getHost(pos)
            responsibleHost.call {_.addUnity(unit)    }
          }

          //      if (id == "3" && clientId2Xp(clientId) >= 10) {
          //        clientId2Xp(clientId) -= 10
          //        val unit = new Commander(pos.x + Random.nextInt(200) - 100, pos.y + Random.nextInt(200) - 100, idObj, clientId, clientId2Color(clientId))
          //        val responsibleHost = HostPool[StgyHost, StgyHostObserver].getHost(pos)
          //        responsibleHost.call {_.elements += idObj -> unit   }
          //      }

          if (id == "8"){
            clientId2Xp(clientId) += 1000
          }
        }
        case None =>
      }
    }
  }
}