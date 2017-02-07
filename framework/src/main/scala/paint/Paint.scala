package paint

import core.observerPattern.Observable
import core.{AbstractMain}
import scala.collection.mutable.ListBuffer


object Paint extends App{

  val main = new AbstractMain[PaintHost, PaintProvider, PaintHostObserver]()
  main.numberOfClient = 100

  val hosts =(0 until 25).map {i => {
    val x = (i %5)*600
    val y = (i /5)*600
    val zone= new SquareZone(x,y,600,600)
    new PaintHost(zone)
  }}


  val hostObserver = new PaintHostObserver()

  main.launch(hosts,hostObserver)


}


class Point(var x: Double, var y : Double, var lineId : String, var order : Int, var color : Array[Int], var thickness : Int) extends Element{

}

class CameraNotifier(var x: Double, var y : Double) extends Element with Observable{
  var camerax : Double = 0
  var cameray : Double = 0
}