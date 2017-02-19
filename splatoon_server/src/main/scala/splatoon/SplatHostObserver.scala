package splatoon

import core.host.{HostObserver, HostPool}

class SplatHostObserver extends HostObserver[SplatClientView] {
  val HP = HostPool[SplatHost, SplatHostObserver]
  var greenCount = 0
  var redCount = 0
  var players = collection.mutable.Map[String, Shooter]()
  var cases = collection.mutable.Map[(Int, Int), Case]()
  var mode = "Team play"

  override def clientInput(id: String, data: String): Unit = {}

  def distribute(shooter: Shooter): Unit = {
    if (mode.equals("Team play")) {
      var newColor = 2
      if (greenCount < redCount) {
        newColor = 1
        greenCount += 1
      }
      else {
        newColor = 0
        redCount += 1
      }
      shooter.color = newColor
    }

    HP.getHost(shooter).call(_.addPlayer(shooter))
    players += shooter.clientId -> shooter
    id2ClientView(shooter.clientId).call(_.setPos(players(shooter.clientId).x, players(shooter.clientId).y))
    id2ClientView(shooter.clientId).call(_.setMiniMap(cases.values.toList))
  }

  def tick() = {
    id2ClientView.foreach{case (id, cv) => {
      if (players.contains(id))
        cv.call(_.setPos(players(id).x, players(id).y))
    }}
  }

  def sendCases(): Unit = {
    id2ClientView.foreach{_._2.call(_.setMiniMap(cases.values.toList))}
  }

  def changeCaseColor(c: Case): Unit = {
    if (cases.contains((c.x.toInt,c.y.toInt))){
      cases((c.x.toInt,c.y.toInt)).color = c.color
    }
    else {
      cases += (c.x.toInt,c.y.toInt) -> c
    }
  }

  def teleportPlayer(player: Shooter, toX: Int, toY: Int): Unit = {
    val cX=  Math.floor(toX/150)*150
    val cY=  Math.floor(toY/150)*150
    if(player.color == cases((cX.toInt, cY.toInt)).color){
      player.x = toX
      player.y = toY
    }
  }

  def setTeamMode(): Unit = {
    mode = "Team Play"
  }

  def setFreeMode(): Unit = {
    mode = "Free for All"
  }
}
