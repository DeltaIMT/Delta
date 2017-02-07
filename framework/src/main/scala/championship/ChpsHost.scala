package championship

import core.host.{Host, HostPool, HostRef}
import core.spatial.{Viewable, Zone}
import play.api.libs.json.Json

import scala.util.Random


class ChpsHost(zone: SquareZone) extends Host(zone) {

  val HP = HostPool[ChpsHost, ChpsHostObserver]
  var elements = collection.mutable.HashMap[String, Element]()
  var elementsFromNeighbours = collection.mutable.HashMap[SquareZone, collection.mutable.HashMap[String, Element]]()
  var neighbours = List[HostRef[ChpsHost]]()
  var clientId2Boat = collection.mutable.HashMap[String, String]()

  def flush() = {
    elements = collection.mutable.HashMap[String, Element]()
  }

  def tick(): Unit = {
    neighbours.foreach(h => h.call(_.elementsFromNeighbours += zone -> elements))

    def distantBoatIterator(func: (SquareZone, Boat) => Unit) = {
      elementsFromNeighbours.foreach { case (z: SquareZone, elmts: collection.mutable.HashMap[String, Element]) => {
        val distant_boats = elmts.values collect { case x: Boat => x }
        distant_boats.foreach(boat => {
          func(z, boat)
        })
      }
      }
    }

    val splashs = elements.values collect { case x: Splash => x }
    val boats = elements.values collect { case x: Boat => x }
    val cannonBalls = elements.values collect { case x: Cannonball => x }

    val otherObstacles = elementsFromNeighbours.values.foldLeft(collection.mutable.HashMap[String, Element]())(_ ++= _).
      values.collect { case x: Obstacle => x }
    val obstacles = elements.values collect { case x: Obstacle => x }

    val allObstacles = obstacles ++ otherObstacles

    splashs.foreach( s => {
      if(s.step)
        elements -= s.id
    })

    boats.foreach(boat => {
      boat.notifyClientViews
      val listNewCannon = boat.step()
      listNewCannon.foreach(c => {
        elements += c.id -> c
      })

      if (boat.isDead)
        elements -= boat.id
    })

    cannonBalls.foreach(cannonball => {
      cannonball.x += cannonball.vx
      cannonball.y += cannonball.vy
      if (cannonball.step){
        elements-= cannonball.id
        val id = Random.alphanumeric.take(10).mkString
        elements += id -> new Splash(cannonball.x, cannonball.y, id)
      }

      boats.foreach(b => {
        b.collisionCannonBall(cannonball) match {
          case Some(damageOnBoat) => {
            damageOnBoat(b)
            elements -= cannonball.id
          }
          case None => {}
        }
      })

      distantBoatIterator((z, b) => {
        b.collisionCannonBall(cannonball) match {
          case Some(damageOnBoat) => {
            HP.hosts(z).call(hr => {
              damageOnBoat(hr.elements(b.id).asInstanceOf[Boat])
            })
            elements -= cannonball.id
          }
          case None => {}
        }
      })
    })


    allObstacles.foreach( obstacle => {
      boats.foreach( boat => {
        boat.getShape.collision2(obstacle.shape) match {
          case Some(collisionPoint ) => {
            boat.health = (boat.health._1 -0.03 , boat.health._2 -0.03 )
            boat.speed += (-  collisionPoint+ boat.position)*0.01
            boat.speed *= 0.8
            boat.u =  (boat.u + boat.v*(-collisionPoint+ boat.position).dotProd(boat.v) * 0.005 ).normalize()
          }
          case None =>
        }
      })
    })

    val pairs = boats.toList.combinations(2)
    pairs.foreach { case List(b1, b2) => {
      if (b1 != b2 && b1.size == b2.size && (Vec(b1.x, b1.y) - Vec(b2.x, b2.y)).length < 100) {
        elements -= b1.id
        elements -= b2.id
        val id = Random.alphanumeric.take(10).mkString
        val newBoat = new Boat((b1.x + b2.x) / 2.0, (b1.y + b2.y) / 2.0, id)
        val totalClient = b1.clientId2position.size + b2.clientId2position.size
        newBoat.getToNextSize(totalClient + totalClient % 2)

        def addOneToAnother(b: Boat) = {
          b.clientViews.foreach(obs => newBoat.sub(obs))
          b.clientId2position.keys.foreach(k => {
            newBoat.addClient(k)
            clientId2Boat(k) = id
          })
        }

        addOneToAnother(b1)
        addOneToAnother(b2)
        elements += id -> newBoat
        newBoat.u = (b1.u + b2.u).normalize()
      }
    }
    }


    elements foreach { elem => {
      val e = elem._2
      if (!zone.contains(e)) {
        HP.getHost(e).call(host => {
          if (e.isInstanceOf[Boat]) {
            val boat = e.asInstanceOf[Boat]
            val clientIds = boat.clientId2position.keys
            clientIds.foreach(cid => {
              host.clientId2Boat += cid -> elem._1
            })
          }
          host.elements += elem._1 -> e
        })
        elements -= elem._1
      }
    }
    }


  }


  override def clientInput(id: String, data: String): Unit = {
    val json = Json.parse(data)
    val x = (json \ "x").toOption
    val y = (json \ "y").toOption
    val b = (json \ "b").toOption


    if (clientId2Boat.contains(id) && elements.contains(clientId2Boat(id))) {
      val boat = elements(clientId2Boat(id)).asInstanceOf[Boat]
      if (x.isDefined && y.isDefined)
        boat.setMoveTarget(id, Vec(x.get.as[Double], y.get.as[Double]))
      if (b.isDefined)
        boat.setShoot(id, b.get.as[Boolean])
    }
  }

  override def getViewableFromZone(id: String, zone: Zone): Iterable[Viewable] = {
    elements.values.filter(e => {
      val ifSplashAlreadySeen  = e match {
        case e:Splash => {
          val isIt = e.listOfKnownClient.contains(id)
          if(!isIt) e.listOfKnownClient ::= id
          !isIt
        }
        case _ => true
      }
      ifSplashAlreadySeen && zone.contains(e)
    })
  }

}
