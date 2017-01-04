package stgy

import core.HostPool
import core.`abstract`.AbstractSpecialHost
import core.user_import.{Observable, Observer}

import scala.util.Random


class IdGiver(val id: String) extends Observable {}
class StgySpecialHost(hostPool: HostPool) extends AbstractSpecialHost[StgyClientView](hostPool) {


  var rand = new Random()

  override def OnConnect(id: String, obs: Observer) = {
    var color = Array(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
    val bowmen = 0 until 100 map { i => new Bowman(100+rand.nextInt(800), 100+rand.nextInt(800), Random.alphanumeric.take(10).mkString, id, color) }
    val flag = new Flag( 100+rand.nextInt(800), 100+rand.nextInt(800), Random.alphanumeric.take(10).mkString, id, color)

    flag::bowmen.toList foreach {
      b =>
        hostPool.getHyperHost(b.x, b.y) method("addUnity", b)
        b.sub(obs)
    }


    //tell the client view what is the client id, this is a hack

    var idGiver = new IdGiver(id)
    idGiver.sub(obs)
    idGiver.notifyClientViews

  }

  override def OnDisconnect(id: String, obs: Observer) = {
    obs.onDisconnect()
  }
}
