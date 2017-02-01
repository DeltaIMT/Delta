package stgy

import core.{HostObserver, HostPool}

class StgyHostObserver(hostPool: HostPool[StgyHost, StgyHostObserver]) extends HostObserver(hostPool){

  var unitys = collection.mutable.HashMap[String, List[String]]()

  def addUnity(unityId: String, clientId: String) {
    if (!unitys.contains(clientId)){
      unitys += clientId -> List[String]()
    }
    unitys(clientId) = unityId::unitys(clientId)
  }


}