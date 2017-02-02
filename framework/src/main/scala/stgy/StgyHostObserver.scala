package stgy

import core.host.HostObserver

class StgyHostObserver extends HostObserver[StgyClientView]{

  var unitys = collection.mutable.HashMap[String, List[String]]()

  def addUnity(unityId: String, clientId: String) {
    if (!unitys.contains(clientId)){
      unitys += clientId -> List[String]()
    }
    unitys(clientId) = unityId::unitys(clientId)
  }

  override def clientInput(id: String, data: String): Unit = {}
}