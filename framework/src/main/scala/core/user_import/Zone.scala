package core.user_import

class Zone(var x :Double,var y: Double,var w : Double,var h : Double){
  def contains(e : Element) =  {
  (e.x <= x + w) && (x<=e.x )&&  (e.y <= y + h) && (y<=e.y)
  }

  def intersectRect(z2: Zone) : Boolean ={

    var bool = false
    var fake = new Element(x,y)

    fake.x = x
    fake.y = y
    bool = bool || z2.contains(fake)
    if(bool) {return true}
    fake.x = x+w
    fake.y = y
    bool = bool || z2.contains(fake)
    if(bool) {return true}
    fake.x = x+w
    fake.y = y+h
    bool = bool || z2.contains(fake)
    if(bool) {return true}
    fake.x = x
    fake.y = y+h
    bool = bool || z2.contains(fake)
    if(bool) {return true}
    fake.x = z2.x
    fake.y = z2.y
    bool = bool || this.contains(fake)
    if(bool) {return true}
    fake.x = z2.x + z2.w
    fake.y = z2.y
    bool = bool || this.contains(fake)
    if(bool) {return true}
    fake.x = z2.x+ z2.w
    fake.y = z2.y+ z2.h
    bool = bool || this.contains(fake)
    if(bool) {return true}
    fake.x = z2.x
    fake.y = z2.y+ z2.h
    bool = bool || this.contains(fake)

  bool
  }
  override def toString: String = "("+x+","+y+","+(x+w)+","+(y+h)+")"
}
