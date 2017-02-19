package splatoon

//color code : 0: green, 1: red, 2: white
class Case(var x : Double, var y : Double, var width : Double, var height : Double, var color : Int) extends Element{

  var idUpToDate = List[String]()

  def changeColor(c : Int) = {
    color = c
    idUpToDate = List[String]()
  }

  def hasBeenSeenBy(id: String) = {
    idUpToDate = id :: idUpToDate
  }

  def hasItBeenSeenBy(id: String) = {
    idUpToDate.contains(id)
  }

}
