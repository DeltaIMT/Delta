package rebound

import core.user_import.Observer
import core.{HostPool, Provider}

import scala.util.Random

class RbndProvider(hostPool: HostPool[RbndHost]) extends Provider[RbndClientView](hostPool) {

  override def OnConnect(id: String, obs: Observer) = {
    val randx = 200 + Random.nextInt(2600)
    val randy = 200 + Random.nextInt(2600)
    val objId = Random.alphanumeric.take(10).mkString
    val newBall = new Ball(randx, randy, 0, 3 , objId, id)
    newBall.sub(obs)
    hostPool.getHyperHost(randx, randy).call(host => {
      host.elements += objId -> newBall
    })
  }

}
