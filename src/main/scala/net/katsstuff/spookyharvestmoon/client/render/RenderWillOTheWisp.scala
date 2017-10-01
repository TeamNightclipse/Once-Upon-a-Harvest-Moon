package net.katsstuff.spookyharvestmoon.client.render

import net.katsstuff.spookyharvestmoon.client.model.ModelWillOTheWisp
import net.katsstuff.spookyharvestmoon.entity.EntityWillOTheWisp
import net.minecraft.client.renderer.entity.{RenderLiving, RenderManager}
import net.minecraft.util.ResourceLocation

class RenderWillOTheWisp(renderManager: RenderManager)
    extends RenderLiving[EntityWillOTheWisp](renderManager, ModelWillOTheWisp, 0.5F) {
  override def getEntityTexture(entity: EntityWillOTheWisp): ResourceLocation = ???
}
