package core.user_import

class Zone(var x :Double,var y: Double,var w : Double,var h : Double){
  def contains(e : Element) = e.x < x + w && x<e.x &&  e.y < y + h && y<e.y
}
