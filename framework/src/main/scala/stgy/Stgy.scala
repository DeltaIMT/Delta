package stgy

import core.AbstractMain
import core.CoreMessage.CallTrace
import kamon.Kamon
import scala.concurrent.duration._
import scala.swing._

object Stgy extends App {

  val main = new AbstractMain[StgyHost, StgyProvider, StgyHostObserver]()
  main.numberOfClient = 100

  val hosts =(0 until 25).map {i => {
    val x = (i %5)*600
    val y = (i /5)*600
    val zone= new SquareZone(x,y,600,600)
    new StgyHost(zone)
  }}





  val hostObserver = new StgyHostObserver()


  main.launch(hosts,hostObserver)

  hosts.map(h => h.zone).foreach( z => {
    main.HP.hosts(z).call( h1 =>{
      val h1zone = h1.zone.asInstanceOf[SquareZone]
      h1.neighbours = main.HP.getHosts(new SquareZone(h1zone.x-1,h1zone.y-1, h1zone.w+2, h1zone.h+2)).filter(_== h1).toList
    })
  })

  main.HP.hosts.values.foreach( hr => main.setInterval(hr,16, h=> h.tick) )



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
