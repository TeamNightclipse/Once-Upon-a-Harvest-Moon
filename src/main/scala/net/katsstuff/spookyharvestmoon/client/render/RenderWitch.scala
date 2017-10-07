package net.katsstuff.spookyharvestmoon.client.render

import net.katsstuff.spookyharvestmoon.client.model.ModelWitch
import net.katsstuff.spookyharvestmoon.entity.EntityWitch
import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.minecraft.client.renderer.entity.{RenderLiving, RenderManager}
import net.minecraft.util.ResourceLocation

class RenderWitch(renderManager: RenderManager)
    extends RenderLiving[EntityWitch](renderManager, ModelWitch, 0.5F) {
  override def getEntityTexture(entity: EntityWitch): ResourceLocation = new ResourceLocation(LibMod.Id, "textures/entity/witch-texturemap.png")

  override def doRender(entity: EntityWitch, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float): Unit = {
    if(entity.state == EntityWitch.State.FirstExplosion) ()
    else super.doRender(entity, x, y, z, entityYaw, partialTicks)
  }
}
