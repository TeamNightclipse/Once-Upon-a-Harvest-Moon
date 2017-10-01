package net.katsstuff.spookyharvestmoon.client.particle

import java.util.concurrent.ThreadLocalRandom
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.world.World

class ParticleGlow(
    val worldIn: World,
    val pos: Vector3,
    val motion: Vector3,
    val r: Float,
    val g: Float,
    val b: Float,
    val scale: Float,
    val lifetime: Int,
    val texture: GlowTexture
) extends AbstractParticleGlow(worldIn, pos, motion)
    with IGlowParticle {
  {
    val colorR = if (r > 1F) r / 255F else r
    val colorG = if (g > 1F) g / 255F else g
    val colorB = if (b > 1F) b / 255F else b
    setRBGColorF(colorR, colorG, colorB)

    particleMaxAge = lifetime
    particleScale = scale
    motionX = motion.x
    motionY = motion.y
    motionZ = motion.z
    particleAngle = 2F * Math.PI.toFloat
  }

  val sprite: TextureAtlasSprite =
    Minecraft.getMinecraft.getTextureMapBlocks.getAtlasSprite(texture.texture.toString)
  this.setParticleTexture(sprite)
  private val initScale = scale

  override def getBrightnessForRender(pTicks: Float) = 255
  override def shouldDisableDepth                    = true
  override def getFXLayer                            = 1

  override def onUpdateGlow(): Unit = {
    super.onUpdateGlow()
    if (ThreadLocalRandom.current.nextInt(6) == 0) particleAge += 1
    val lifeCoeff = particleAge.toFloat / particleMaxAge.toFloat
    particleScale = initScale - initScale * lifeCoeff
    particleAlpha = 1F - lifeCoeff
    prevParticleAngle = particleAngle
    particleAngle += 1F
  }

  override def alive: Boolean = this.particleAge < this.particleMaxAge
  override def isAdditive  = true
  override def ignoreDepth = false
}
