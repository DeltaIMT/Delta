package championship


import core.Delta
import core.host.HostPool
import kamon.Kamon

import scala.swing._
import scala.util.Random

object Chps extends App {

  val main = new Delta[ChpsHost, ChpsProvider,ChpsHostObserver]()
  main.numberOfClient = 100

  val hosts =(0 until 25).map {i => {
    val x = (i %5)*600
    val y = (i /5)*600
    val zone= new SquareZone(x,y,600,600)
    new ChpsHost(zone)
  }}

  val hostObserver = new ChpsHostObserver()

  main.launch(hosts,hostObserver)

  hosts.map(h => h.zone).foreach( z => {
    main.HP.hosts(z).call( h1 =>{
      val h1zone = h1.zone.asInstanceOf[SquareZone]
      h1.neighbours = main.HP.getHosts(new SquareZone(h1zone.x-10,h1zone.y-10, h1zone.w+20, h1zone.h+20)).filter(_!=  main.HP.hosts(z)).toList
    })
  })

  main.HP.hosts.values.foreach( hr =>  main.setHostInterval(hr,16, _.tick()) )

  HostPool[ChpsHost,ChpsHostObserver].hosts.foreach {
    case (zone,hr) =>  {
    (0 until 3).toList.foreach( k => {
      val objId = Random.alphanumeric.take(10).mkString
      hr.call( h => {
        val sz= zone.asInstanceOf[SquareZone]
       val middle = Vec(sz.x,sz.y)
        h.elements += objId -> new Obstacle(
          middle.x + Random.nextInt(sz.w.toInt) ,
          middle.y + Random.nextInt(sz.h.toInt) ,
          objId)
      })
    })
  }}

  main.setHostObserverInterval(main.HP.hostObserver,32, h=> h.tick)

  val ui = new UI

  def shutdown = {
    println("framework shutdown")
    main.actorSystem.terminate()
    Kamon.shutdown()
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
      }
      contents += Button("Flush") {
        main.HP.hosts.values foreach (_.call(_.flush()))
      }
      contents += Button("Close") {
        shutdown
        sys.exit(0)
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }
  ui.visible = true
}
