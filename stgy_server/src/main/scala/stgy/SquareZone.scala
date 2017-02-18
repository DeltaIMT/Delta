package stgy

import core.spatial.{Viewable, Zone}

class SquareZone(var x :Double,var y: Double,var w : Double,var h : Double) extends Zone{

  override def contains(e : Viewable) = e match {
    case e:Element =>
      (e.x <= x + w) && (x<=e.x )&&  (e.y <= y + h) && (y<=e.y)
    case _ => false
}

  override def intersect(z2: Zone) : Boolean =   z2 match {
      case z2:SquareZone => {
        var bool = false
        class FakeElement(var x: Double, var y :Double) extends Element{}
        var fake = new FakeElement(x,y)
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
      case _ => false
    }
}
