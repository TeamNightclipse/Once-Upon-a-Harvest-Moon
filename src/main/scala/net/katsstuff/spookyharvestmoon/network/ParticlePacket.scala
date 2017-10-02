package net.katsstuff.spookyharvestmoon.network

import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, ParticleUtil}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.network.scalachannel.{ClientMessageHandler, MessageConverter}
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient

case class ParticlePacket(
    pos: Vector3,
    motion: Vector3,
    r: Float,
    g: Float,
    b: Float,
    scale: Float,
    lifetime: Int,
    texture: GlowTexture
)
object ParticlePacket {

  implicit val converter: MessageConverter[ParticlePacket] = shapeless.cachedImplicit

  implicit val handler: ClientMessageHandler[ParticlePacket, Unit] = new ClientMessageHandler[ParticlePacket, Unit] {
    override def handle(netHandler: NetHandlerPlayClient, a: ParticlePacket): Option[Unit] = {
      scheduler.addScheduledTask(ParticlePacketRunnable(netHandler, a))
      None
    }
  }
}

case class ParticlePacketRunnable(server: NetHandlerPlayClient, packet: ParticlePacket) extends Runnable {
  override def run(): Unit =
    ParticleUtil.spawnParticleGlow(
      Minecraft.getMinecraft.player.world,
      packet.pos,
      packet.motion,
      packet.r,
      packet.g,
      packet.b,
      packet.scale,
      packet.lifetime,
      packet.texture
    )
}
