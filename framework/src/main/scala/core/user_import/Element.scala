package core.user_import

import core.HyperHost

trait Element {

  var x:Double
  var y:Double
  def update(h : HyperHost[_], s : String){
    h.host ! Set(s, this)
  }

  override def toString: String = "("+x+","+y+")"
}
