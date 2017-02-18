package core.spatial
// define an area, by defining 2 methods : if a viewable is inside a zone ; if there is a commun area between 2 zone.
trait Zone {

  def contains(e : Viewable) : Boolean
  def intersect(z : Zone) : Boolean

}
