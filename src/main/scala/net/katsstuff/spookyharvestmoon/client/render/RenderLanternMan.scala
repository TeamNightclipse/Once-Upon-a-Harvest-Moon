package net.katsstuff.spookyharvestmoon.client.render

import net.katsstuff.spookyharvestmoon.client.model.ModelLanternMan
import net.katsstuff.spookyharvestmoon.entity.EntityLanternMan
import net.minecraft.client.renderer.entity.{RenderLiving, RenderManager}
import net.minecraft.util.ResourceLocation

class RenderLanternMan(renderManager: RenderManager)
    extends RenderLiving[EntityLanternMan](renderManager, ModelLanternMan, 0.5F) {
  override def getEntityTexture(entity: EntityLanternMan): ResourceLocation = ???
}
