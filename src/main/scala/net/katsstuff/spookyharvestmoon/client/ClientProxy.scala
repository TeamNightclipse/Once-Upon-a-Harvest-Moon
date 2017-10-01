package net.katsstuff.spookyharvestmoon.client

import net.katsstuff.spookyharvestmoon.CommonProxy
import net.katsstuff.spookyharvestmoon.client.particle.{IGlowParticle, ParticleRenderer}

class ClientProxy extends CommonProxy {

  val particleRenderer = new ParticleRenderer

  override def addParticle(particle: IGlowParticle): Unit = {
    particleRenderer.addParticle(particle)
  }
}
