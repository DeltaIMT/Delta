package stgy

import core.user_import.{Observable, Observer}
import core.{HostPool, Provider}

import scala.util.Random


class IdGiver(val id: String,val x: Double, val y :Double) extends Observable {}
class StgyProvider(hostPool: HostPool) extends Provider[StgyClientView](hostPool) {


  var rand = new Random()

  override def OnConnect(id: String, obs: Observer) = {
    var color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
    val randx = 200+rand.nextInt(2600)
    val randy = 200+rand.nextInt(2600)
    val numberOfStartUnit = 5
    val sqrt = math.sqrt(numberOfStartUnit).toInt
    val bowmen = 0 until 20 map { i => new Bowman(randx + 40*(i%sqrt) , randy + 40*(i/sqrt), Random.alphanumeric.take(10).mkString, id, color) }
    val flag = new Flag( randx, randy, Random.alphanumeric.take(10).mkString, id, color)
    val com = new Commander(randx, randy, Random.alphanumeric.take(10).mkString, id, color)
    val spawned : List[Unity] = com::bowmen.toList //flag::

    spawned foreach {
      b =>
        hostPool.getHyperHost(b.x, b.y) method("addUnity", b)
        b.sub(obs)
    }


    //tell the client view what is the client id, this is a hack

    var idGiver = new IdGiver(id,randx,randy)
    idGiver.sub(obs)
    idGiver.notifyClientViews

  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
  }
}
