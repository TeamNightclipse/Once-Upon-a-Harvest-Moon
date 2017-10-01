package net.katsstuff.spookyharvestmoon.network

import io.netty.buffer.ByteBuf
import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, ParticleUtil}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.network.scalachannel.{MessageConverter, MessageHandler, ServerMessageHandler}
import net.minecraft.client.Minecraft
import net.minecraft.network.NetHandlerPlayServer

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

  implicit val converter: MessageConverter[ParticlePacket] = new MessageConverter[ParticlePacket] {
    override def toBytes(a: ParticlePacket, buf: ByteBuf): Unit = {
      MessageConverter.writeBytes(a.pos, buf)
      MessageConverter.writeBytes(a.motion, buf)
      buf.writeFloat(a.r)
      buf.writeFloat(a.g)
      buf.writeFloat(a.b)
      buf.writeFloat(a.scale)
      buf.writeInt(a.lifetime)
      buf.writeInt(GlowTexture.idOf(a.texture))
    }

    override def fromBytes(buf: ByteBuf): ParticlePacket = ParticlePacket(
      MessageConverter.readBytes(buf),
      MessageConverter.readBytes(buf),
      buf.readFloat(),
      buf.readFloat(),
      buf.readFloat(),
      buf.readFloat(),
      buf.readInt(),
      GlowTexture
        .fromId(buf.readInt())
        .getOrElse(throw new IllegalArgumentException("Got wrong id for particle packet"))
    )
  }

  implicit val handler: MessageHandler[ParticlePacket, Unit] = new ServerMessageHandler[ParticlePacket, Unit] {
    override def handle(netHandler: NetHandlerPlayServer, a: ParticlePacket): Option[Unit] = {
      scheduler.addScheduledTask(ParticlePacketRunnable(netHandler, a))
      None
    }
  }
}

case class ParticlePacketRunnable(server: NetHandlerPlayServer, packet: ParticlePacket) extends Runnable {
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
