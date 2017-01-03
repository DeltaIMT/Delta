package stgy

import akka.actor.ActorRef
import core.HostPool
import core.`abstract`.AbstractClientView
import core.user_import.Zone

class StgyClientView(hostPool: HostPool, client: ActorRef) extends AbstractClientView(hostPool, client) {
  var pos = Vec(1500, 1500)
  var id = ""
  var hash  = collection.mutable.HashMap[String,Int]()
  var numberOfUnit = 1.0

  var min = Vec(0,0)
  var max = Vec(1920,1080)


  override def dataToViewZone(): List[Zone] = List(new Zone(pos.x - 1500, pos.y - 1500, 3000, 3000))

  override def onNotify(any: Any): Unit = {

    any match {
      case e: IdGiver => id = e.id
      case bowman: Bowman => {

        min.x = math.min( min.x , bowman.x)
        min.y = math.min( min.y , bowman.y)
        max.x = math.max( max.x , bowman.x)
        max.y = math.max( max.y , bowman.y)


        if(hash.contains(bowman.id))
          hash(bowman.id) = 5
        else
          hash += bowman.id -> 5
      }
      case _ => {
        println("notify not matched")
      }
    }
  }

  override def onDisconnect(any: Any): Unit = {
    //  hostPool.getHyperHost(x, y).exec(l => l -= idBall)
  }

  override def fromListToClientMsg(list: List[Any]) = {

    pos = (max *0.5) +( min *0.5)
    min += Vec(10,10)
    max -= Vec(10,10)
    hash.keys.foreach( k =>  hash(k) = hash(k)-1 )
    hash= hash.filter( (pair) => pair._2>0)
    numberOfUnit = hash.size.toDouble
    val listString = list.map {

      case e: Bowman => {
        s"""{"type":"bowman","id":"${e.id}","mine":${id == e.clientId},"health":"${e.health}","x":"${e.x.toInt}","y":"${e.y.toInt}","color":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
      }
      case e: Arrow => {
        s"""{"type":"arrow","id":"${e.id}","x":"${e.x.toInt}","y":"${e.y.toInt}","color":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
      }
      case e: Flag => {
        s"""{"type":"flag","id":"${e.id}","spawning":"${1.0 - e.canSpawnIn/e.frameToSpawn.toFloat}","possessing":${e.possessing},"x":"${e.x.toInt}","y":"${e.y.toInt}","color":[${e.color(0)},${e.color(1)},${e.color(2)}]}"""
      }
      case e => "NOT ELEMENT : " + e
    } ++ List(
      s"""{"type":"camera","x":"${pos.x.toInt}","y":"${pos.y.toInt}"}""",
      s"""{"type":"other","n":"${numberOfUnit}"}""")
    val string = listString.mkString("[", ",", "]")
    // println(string)
    string
  }
}
