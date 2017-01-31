package stgy

import core.{HostObserver, HostPool}

class StgyHostObserver(hostPool: HostPool[StgyHost, StgyHostObserver]) extends HostObserver(hostPool){

  var unitys = collection.mutable.HashMap[String, String]()

  def AddUnity(unityId: String, clientId: String) {
    unitys += unityId -> clientId
  }

}
