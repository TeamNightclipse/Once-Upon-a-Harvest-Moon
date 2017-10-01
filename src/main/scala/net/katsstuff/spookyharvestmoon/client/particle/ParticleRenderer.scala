package net.katsstuff.spookyharvestmoon.client.particle

import scala.collection.mutable.ArrayBuffer

import org.lwjgl.opengl.GL11

import net.katsstuff.spookyharvestmoon.client.lib.LibParticleTextures
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.{ActiveRenderInfo, GlStateManager, Tessellator}
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraftforge.client.event.{RenderWorldLastEvent, TextureStitchEvent}
import net.minecraftforge.fml.common.eventhandler.{EventPriority, SubscribeEvent}
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class ParticleRenderer {

  private val particles = ArrayBuffer.empty[IGlowParticle]

  @SubscribeEvent
  def onTextureStitch(event: TextureStitchEvent): Unit = {
    event.getMap.registerSprite(LibParticleTextures.ParticleGlint)
    event.getMap.registerSprite(LibParticleTextures.ParticleGlow)
    event.getMap.registerSprite(LibParticleTextures.ParticleMote)
    event.getMap.registerSprite(LibParticleTextures.ParticleStar)
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  def onTick(event: TickEvent.ClientTickEvent): Unit = if (event.side == Side.CLIENT) updateParticles()

  @SubscribeEvent @SideOnly(Side.CLIENT)
  def onRenderAfterWorld(event: RenderWorldLastEvent): Unit = {
    GlStateManager.pushMatrix()
    renderParticles(event.getPartialTicks)
    GlStateManager.popMatrix()
  }

  private def updateParticles(): Unit = {
    val toRemove = for (particle <- particles) yield {
      if (particle.alive) {
        particle.onUpdateGlow()
        None
      } else Some(particle)
    }

    particles --= toRemove.flatten
  }

  private def renderParticles(partialTicks: Float): Unit = {
    val player = Minecraft.getMinecraft.player
    if (player != null) {
      val f  = ActiveRenderInfo.getRotationX
      val f1 = ActiveRenderInfo.getRotationZ
      val f2 = ActiveRenderInfo.getRotationYZ
      val f3 = ActiveRenderInfo.getRotationXY
      val f4 = ActiveRenderInfo.getRotationXZ

      Particle.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
      Particle.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
      Particle.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks
      Particle.cameraViewDir = player.getLook(partialTicks)

      GlStateManager.enableAlpha()
      GlStateManager.enableBlend()
      GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0)
      GlStateManager.disableCull()
      GlStateManager.disableLighting()
      GlStateManager.depthMask(false)

      Minecraft.getMinecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
      val tess   = Tessellator.getInstance
      val buffer = tess.getBuffer

      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if !particle.isAdditive) {
        particle.renderParticleGlow(buffer, player, partialTicks, f, f4, f1, f2, f3)
      }
      tess.draw()

      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if particle.isAdditive) {
        particle.renderParticleGlow(buffer, player, partialTicks, f, f4, f1, f2, f3)
      }
      tess.draw()

      GlStateManager.disableDepth()
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
      for (particle <- particles if particle.ignoreDepth) {
        particle.renderParticleGlow(buffer, player, partialTicks, f, f4, f1, f2, f3)
      }
      tess.draw()

      GlStateManager.enableDepth()
      GlStateManager.enableCull()
      GlStateManager.depthMask(true)
      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
      GlStateManager.disableBlend()
      GlStateManager.alphaFunc(516, 0.1F)
    }
  }

  def addParticle(particle: IGlowParticle): Unit = particles += particle
}
