package stgy

import core.host.HostObserver

class StgyHostObserver extends HostObserver[StgyClientView]{


  var clientId2Xp = collection.mutable.HashMap[String, Double]()
  var unitys = collection.mutable.HashMap[String, List[String]]()

  def gainxp(clientId:String, xp : Double) = {
    if(clientId2Xp.contains(clientId))
      clientId2Xp(clientId) +=  xp
    else
      clientId2Xp += clientId -> xp
  }


  def addUnity(unityId: String, clientId: String) {
    if (!unitys.contains(clientId)){
      unitys += clientId -> List[String]()
    }
    unitys(clientId) = unityId::unitys(clientId)
  }

  def tick = {
    id2ClientView.foreach { case (id, cv) => {
      cv.call(c => c.xp = clientId2Xp(id))
      println("sending xp to cv " + cv + " " + clientId2Xp(id))
    }}
  }

  override def clientInput(id: String, data: String): Unit = {}
}