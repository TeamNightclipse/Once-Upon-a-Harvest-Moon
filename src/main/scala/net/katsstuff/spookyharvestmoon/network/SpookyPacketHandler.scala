package net.katsstuff.spookyharvestmoon.network

import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.katsstuff.spookyharvestmoon.network.scalachannel.ScalaNetworkWrapper

object SpookyPacketHandler extends ScalaNetworkWrapper(LibMod.Id) {
  //Not used yet
  def load(): Unit = {
    registerMessages {
      for {
        _ <- init
      } yield ()
    }
  }
}
