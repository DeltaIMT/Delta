package demo

import core.clientView.ClientView
import core.spatial.Zone

class DemoClientView(id : String) extends ClientView(id) {


  override def dataToViewZone(): Zone = new SquareZone(0,0,600,600)

  override def onNotify(any: Any): Unit ={}

  override def fromListToClientMsg(list: List[Any]) = {
    Left(list.asInstanceOf[List[Ball]] map (_.toJson) mkString("[",",","]"))
  }
}
