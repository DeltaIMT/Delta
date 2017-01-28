package rebound

import core.{Host, HostPool}
import core.user_import.Zone

class RbndHost(hostPool: HostPool[RbndHost], zone: Zone) extends Host(hostPool, zone) {


 override def tick = {

   val balls = elements.values.collect( case x:Ball => x)




  }

  override def clientInput(id: String, data: String): Unit = {



  }


}
