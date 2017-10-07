package net.katsstuff.spookyharvestmoon.client.particle

import java.util.Random

import net.katsstuff.spookyharvestmoon.SpookyHarvestMoon
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.entity.EntityWillOTheWisp
import net.katsstuff.spookyharvestmoon.network.{ParticlePacket, SpookyPacketHandler}
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

  def spawnPoff(world: World, pos: Vector3, color: Int): Unit = {
    val basePos = pos
    val r       = (color >> 16 & 255) / 255.0F
    val g       = (color >> 8 & 255) / 255.0F
    val b       = (color & 255) / 255.0F
    val size    = 0.4F
    for (_ <- 0 until 32) {
      val motion = Vector3.randomDirection
      val pos    = basePos.offset(motion, Math.random())
      SpookyHarvestMoon.proxy.spawnParticleGlow(world, pos, motion / 10D, r, g, b, size * 10F, 40, GlowTexture.Mote)
    }
  }

  def spawnPoffPacket(world: World, pos: Vector3, color: Int): Unit = {
    val basePos = pos
    val r       = (color >> 16 & 255) / 255.0F
    val g       = (color >> 8 & 255) / 255.0F
    val b       = (color & 255) / 255.0F
    val size    = 0.4F
    for (_ <- 0 until 32) {
      val motion = Vector3.randomDirection
      val pos    = basePos.offset(motion, Math.random())
      ParticleUtil.spawnParticleGlowPacket(world, pos, motion / 10D, r, g, b, size * 10F, 40, GlowTexture.Mote, 32)
    }
  }
}
