package core.host

import core.spatial.{Viewable, Zone}

abstract class Host(val zone: Zone) extends InputReceiver{

  def getViewableFromZone(zone : Zone) : Iterable[Viewable]



}
