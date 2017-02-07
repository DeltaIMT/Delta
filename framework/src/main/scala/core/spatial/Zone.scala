package core.spatial

trait Zone {

  def contains(e : Viewable) : Boolean
  def intersect(z : Zone) : Boolean

}
