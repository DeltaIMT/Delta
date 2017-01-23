package stgy

import core.AbstractMain

object Stgy extends App {

  val main = new AbstractMain[StgyHost,StgyProvider]()
  main.numberOfClient = 100
  main.launch
  main.createUI
}
