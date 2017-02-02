package test

import core2.HostPool
import core2.host.Host
import core2.spatial.{Viewable, Zone}

class HostTest(zone: Zone) extends Host(zone) {

  val HP = HostPool[HostTest, HostObsTest]

  var list = List[ViewableTest]()

  def tick = {
    list.foreach(v => {
      v.x += 1
      println("v : " + v + " x : " + v.x)
      if (!zone.contains(v)) {
        HP.getHost(v).call(h => h.list = v :: h.list)
        list = list.filter(x => x != v)
        println("Teleporting v " + v )
      }
    }
    )
  }

  override def getViewableFromZone(zone: Zone): Iterable[Viewable] = list

  override def clientInput(id: String, data: String): Unit = {}
}
