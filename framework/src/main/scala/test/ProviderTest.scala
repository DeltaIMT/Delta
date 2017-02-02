package test

import core2.Provider
import core2.spatial.Zone


class ProviderTest extends Provider {
  override def hostsStringToZone(s: String): Zone = new SquareZone(0,0,0,0)
}
