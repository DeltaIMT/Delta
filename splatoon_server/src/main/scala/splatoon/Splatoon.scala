package splatoon

import core.AbstractMain

import scala.swing._

object Splatoon extends App{

  val main = new AbstractMain[SplatHost, SplatProvider, SplatHostObserver]()
  main.numberOfClient = 100

  val hosts =(0 until 25).map {i => {
    val x = (i %5)*600
    val y = (i /5)*600
    val zone= new SquareZone(x,y,600,600)
    new SplatHost(zone, i)
  }}

  val hostObserver = new SplatHostObserver

  main.launch(hosts, hostObserver)

  hosts.map(h => h.zone).foreach( z => {
    main.HP.hosts(z).call( h1 =>{
      val h1zone = h1.zone.asInstanceOf[SquareZone]
      h1.neighbours = main.HP.getHosts(new SquareZone(h1zone.x-10,h1zone.y-10, h1zone.w+20, h1zone.h+20)).filter(_!= h1).toList
    })
  })

  main.HP.hosts.values.foreach( hr =>  main.setHostInterval(hr,16, h=> h.tick) )
  main.setHostObserverInterval(main.HP.hostObserver,16, h=> h.tick)
  main.setHostObserverInterval(main.HP.hostObserver,1000, h=>h.sendCases)

  val ui = new UI

  def shutdown = {
    println("framework shutdown")
    main.actorSystem.terminate()
    println("Done")
  }

  class UI extends MainFrame {
    title = "GUI for Delta Server"
    contents = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Server")
      contents += Swing.VStrut(10)
      contents += Swing.Glue
      contents += Button("Shutdown") {
        shutdown
        sys.exit(0)
      }
      contents += Button("Team play") {
        main.HP.hostObserver.call(_.setTeamMode())
      }
      contents += Button("Free for All") {
        main.HP.hostObserver.call(_.setFreeMode())
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }
  ui.visible = true
}
