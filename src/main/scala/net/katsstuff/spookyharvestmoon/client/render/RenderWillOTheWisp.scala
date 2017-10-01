package net.katsstuff.spookyharvestmoon.client.render

import org.lwjgl.opengl.GL11

import net.katsstuff.spookyharvestmoon.client.helper.RenderHelper
import net.katsstuff.spookyharvestmoon.client.model.EmptyModel
import net.katsstuff.spookyharvestmoon.entity.EntityWillOTheWisp
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.{RenderLiving, RenderManager}
import net.minecraft.util.ResourceLocation

class RenderWillOTheWisp(renderManager: RenderManager)
    extends RenderLiving[EntityWillOTheWisp](renderManager, EmptyModel, 0.5F) {
  override def doRender(
      entity: EntityWillOTheWisp,
      x: Double,
      y: Double,
      z: Double,
      entityYaw: Float,
      partialTicks: Float
  ): Unit = {
    super.doRender(entity, x, y, z, entityYaw, partialTicks)
    GlStateManager.pushMatrix()

    val pitch = entity.rotationPitch
    val yaw   = entity.rotationYaw
    val size  = 0.3F
    val color = EntityWillOTheWisp.formToColor(entity.form)
    val alpha = 0.35F

    GlStateManager.translate(x, y + 0.2, z)
    GlStateManager.rotate(-yaw, 0F, 1F, 0F)
    GlStateManager.rotate(-pitch, 1F, 0F, 0F)
    GlStateManager.scale(size, size, size)

    GlStateManager.disableLighting()

    RenderHelper.drawSphere(0xFFFFFF, 1F)

    GlStateManager.enableBlend()
    GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE)
    GlStateManager.depthMask(false)
    GlStateManager.scale(1.1F, 1.1F, 1.1F)
    RenderHelper.drawSphere(color, alpha)
    GlStateManager.depthMask(true)
    GlStateManager.disableBlend()

    GlStateManager.enableLighting()
    GlStateManager.popMatrix()
  }
  override def getEntityTexture(entity: EntityWillOTheWisp): ResourceLocation = ???
}
