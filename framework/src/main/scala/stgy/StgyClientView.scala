package stgy

import akka.actor.ActorRef
import core.HostPool
import core.`abstract`.AbstractClientView
import core.user_import.Zone

class StgyClientView(hostPool: HostPool[StgyHost], client: ActorRef) extends AbstractClientView(hostPool, client) {
  var pos = Vec(1500, 1500)
  var id = ""
  var hash  = collection.mutable.HashMap[String,Int]()
  var numberOfUnit = 1.0

  var hashIdColor= collection.mutable.HashMap[String, Boolean]()

  var hashIdChangeHost = collection.mutable.HashMap[String, Boolean]()
  var hashTime = collection.mutable.HashMap[String, Int]()


  var min = Vec(0,0)
  var max = Vec(3000,3000)


  override def dataToViewZone(): List[Zone] = List(new Zone(pos.x - 1500, pos.y - 1500, 3000, 3000))

  override def onNotify(any: Any): Unit = {

    any match {
      case e: IdGiver => {id = e.id; min= Vec(e.x-100,e.y-100) ; max= Vec(e.x+100,e.y+100)}
      case unit: Unity => {

        min.x = math.min( min.x , unit.x)
        min.y = math.min( min.y , unit.y)
        max.x = math.max( max.x , unit.x)
        max.y = math.max( max.y , unit.y)


        if(hash.contains(unit.id))
          hash(unit.id) = 5
        else
          hash += unit.id -> 5
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

    // hashTime uploaded each time "fromListToClientMsg is called :
    // if the element is unknown, we added it inside hashTime, else we bring its value to 5
    val unitys = list.filter( x => x.isInstanceOf[Unity]).asInstanceOf[List[Unity]]
    unitys.foreach( u => {
      if( !hashIdColor.contains(u.id) )
        hashIdColor+= u.id -> false
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
    pos = (max *0.5) +( min *0.5)
    min += Vec(100,100)
    max -= Vec(100,100)
    hash.keys.foreach( k =>  hash(k) = hash(k)-1 )

    // while processing "fromListToClientMsg", we decrease the value of each key we have
    hashTime.keys.foreach( k =>  hashTime(k) = hashTime(k)-1 )

    hash= hash.filter( (pair) => pair._2>0)
    numberOfUnit = hash.size.toDouble

    val listString = list.map {
      case u : Unity =>{
        val colorString = if ((!hashIdColor(u.id))||(hashIdChangeHost(u.id))) s""","color":[${u.color(0)},${u.color(1)},${u.color(2)}]""" else ""
//        if ((!hashIdColor(u.id))||(hashIdChangeHost(u.id)))
//          println("color needed")


        u match {
          case e: Commander => {
            s"""{"type":"com","id":"${e.id}","spawning":"${1.0 - e.canSpawnIn / e.frameToSpawn.toFloat}","xp":"${e.xp}","mine":${id == e.clientId},"health":"${e.health}","x":"${e.x.toInt}","y":"${e.y.toInt}"${colorString}}"""
          }
          case e: Bowman => {
            s"""{"type":"bowman","id":"${e.id}","mine":${id == e.clientId},"health":"${e.health}","xp":"${e.xp}","x":"${e.x.toInt}","y":"${e.y.toInt}"${colorString}}"""
          }
          case e: Arrow => {
            s"""{"type":"arrow","id":"${e.id}","x":"${e.x.toInt}","y":"${e.y.toInt}"}"""
          }
          case e: Flag => {
            s"""{"type":"flag","id":"${e.id}","spawning":"${1.0 - e.canSpawnIn / e.frameToSpawn.toFloat}","possessing":${e.possessing},"x":"${e.x.toInt}","y":"${e.y.toInt}"${colorString}}"""
          }
        }
      }
      case e => "NOT ELEMENT : " + e
    } ++ List(
      s"""{"type":"camera","id":"0","x":"${pos.x.toInt}","y":"${pos.y.toInt}"}""",
      s"""{"type":"other","id":"1","n":"${numberOfUnit}"}""")
    val string = listString.mkString("[", ",", "]")
    // println(string)
    string
  }
}
