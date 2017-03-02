package splash

import core.clientView.ClientView
import core.host.HostPool
import core.spatial.Zone

class SplatClientView(id : String) extends ClientView(id) {
  val HP = HostPool[SplatHost, SplatHostObserver]
  var pos = Vec(1500, 1500)
  var miniMapCases = List[Case]()

  var unitTeam = 0
  var counterMiniMap = 0
  var minimapString = List[String]()

  override def dataToViewZone(): Zone = new SquareZone(pos.x - 1920/2, pos.y - 1080/2, 1920, 1080)

  override def onNotify(any: Any): Unit = {}

  override def fromListToClientMsg(list: List[Any]) = {
    counterMiniMap += 1

    val shooters = list.filter(_.isInstanceOf[Shooter]).asInstanceOf[List[Shooter]]

    val cases = list.filter(_.isInstanceOf[Case]).asInstanceOf[List[Case]]

    val balls = list.filter(_.isInstanceOf[Ball]).asInstanceOf[List[Ball]]

    val caseString = cases.map(c => s"""{"type":"case","x":"${c.x.toInt}","y":"${c.y.toInt}","color":"${c.color}"}""")

    val ballString = balls.map(b => s"""{"type":"ball","id":"${b.id}","x":"${b.x.toInt}","y":"${b.y.toInt}","color":"${b.color}"}""")

    val shooterString = shooters.map (s =>
      s"""{"type":"shooter","id":"${s.id}","mine":${id == s.clientId},"x":"${s.x.toInt}","y":"${s.y.toInt}","color":"${s.color}","hit":"${s.hit}"}"""
    )

    var listString = shooterString++ caseString ++ ballString ++ List(s"""{"type":"camera","id":"0","x":"${pos.x.toInt}","y":"${pos.y.toInt}"}""")

    if(counterMiniMap==60){
      minimapString = miniMapCases.map(c => s"""{"type":"map","x":"${c.x.toInt}","y":"${c.y.toInt}","color":"${c.color}"}""")
      listString ++= minimapString
      counterMiniMap = 0
    }

    val string = listString.mkString("[", ",", "]")
    Left(string)
  }

  def setPos(newX: Double, newY: Double): Unit = {
    pos.x = newX
    pos.y = newY
  }

  def setMiniMap(m: List[Case]): Unit = {
    miniMapCases = m
  }

}
