package net.katsstuff.spookyharvestmoon.client.particle

import java.util.Random

import net.katsstuff.spookyharvestmoon.SpookyHarvestMoon
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.network.SpookyPacketHandler
import net.katsstuff.spookyharvestmoon.network.scalachannel.TargetPoint
import net.minecraft.client.Minecraft
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ParticleUtil {
  private val random  = new Random
  private var counter = 0
  @SideOnly(Side.CLIENT)
  def spawnParticleGlow(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      texture: GlowTexture
  ): Unit = {
    counter += random.nextInt(3)
    if (counter % (if (Minecraft.getMinecraft.gameSettings.particleSetting == 0) 1
                   else 2 * Minecraft.getMinecraft.gameSettings.particleSetting) == 0) {
      SpookyHarvestMoon.proxy.addParticle(new ParticleGlow(world, pos, motion, r, g, b, scale, lifetime, texture))
    }
  }
  def spawnParticleGlowPacket(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      `type`: GlowTexture,
      range: Int
  ): Unit = {
    val message = new ParticlePacket(pos, motion, r, g, b, scale, lifetime, `type`)
    val point   = TargetPoint(world.provider.getDimension, pos.x, pos.y, pos.z, range)
    SpookyPacketHandler.sendToAllAround(message, point)
  }
}
