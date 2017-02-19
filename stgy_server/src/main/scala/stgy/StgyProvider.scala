package stgy


import core.host.HostPool
import core.observerPattern.{Observable, Observer}
import core.provider.Provider
import core.spatial.Zone
import play.api.libs.json.{JsArray, Json}
import stgy.StgyTypes.Color

import scala.util.Random




class StgyProvider extends Provider[StgyClientView] {
  val HP = HostPool[StgyHost, StgyHostObserver]
  var rand = new Random()

  override def OnConnect(id: String, obs: Observer) = {

    var a = Random.nextDouble()*math.Pi*2
    def sin(angle: Double, phase : Double) : Int = (123.0+122.0*math.sin(angle + phase)).toInt
    var color= Color( sin(a,0), sin(a, math.Pi/3) , sin(a , 2*math.Pi/3))

    val randx = 200+rand.nextInt(2600)
    val randy = 200+rand.nextInt(2600)
    val numberOfStartUnit = 4
    val sqrt = math.sqrt(numberOfStartUnit).toInt
    val swordman = 0 until numberOfStartUnit map { i =>  new Swordman(randx + 40*(i%sqrt) , randy + 40*(i/sqrt), Random.alphanumeric.take(10).mkString, id, color)    }
    val bowmen = 0 until numberOfStartUnit map { i => new Bowman(randx + 40*(i%sqrt) , randy + 40*(i/sqrt), Random.alphanumeric.take(10).mkString, id, color) }
    val com = new Commander(randx, randy, Random.alphanumeric.take(10).mkString, id, color)
    val spawned : List[Unity] = com::bowmen.toList :::swordman.toList

    HP.hostObserver.call(_.addClient(id,color))


    spawned foreach {
      b =>
        HP.getHost(Vec(b.x, b.y)).call( i => i.addUnity(b)  )
        b.sub(obs)
    }

  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
    HP.hostObserver.call(_.deleteClient(id))
  }

  override def hostsStringToZone(s: String): Option[Zone] = {
    //println(s)
   val json = Json.parse(s).asInstanceOf[JsArray].value
    //println(json)
    val x = json(0).as[Int]
    if(x == -1)
      return None
    val y = json(1).as[Int]
    val w = json(2).as[Int]
    val h = json(3).as[Int]
    Option(new SquareZone(x,y,w,h))
  }

}
