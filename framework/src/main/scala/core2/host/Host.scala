package core2.host

import core2.spatial.{Viewable, Zone}
import core2.Zone

abstract class Host(val zone: Zone) extends InputReceiver{

  def getViewableFromZone(zone : Zone) : Iterable[Viewable]



}
