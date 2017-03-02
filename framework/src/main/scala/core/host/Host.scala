package core.host

import core.spatial.{Viewable, Zone}

//The class that will extends Host will contain the implementations for each zones
//It is aggregated by the HostActor that call its functions
abstract class Host(val zone: Zone) extends InputReceiver{

  def getViewableFromZone(id: String , zone : Zone) : Iterable[Viewable]



}
