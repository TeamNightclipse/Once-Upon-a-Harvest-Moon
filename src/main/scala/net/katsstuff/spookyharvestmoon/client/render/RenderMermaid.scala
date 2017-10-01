package net.katsstuff.spookyharvestmoon.client.render

import net.katsstuff.spookyharvestmoon.client.model.ModelMermaid
import net.katsstuff.spookyharvestmoon.entity.EntityMermaid
import net.minecraft.client.renderer.entity.{RenderLiving, RenderManager}
import net.minecraft.util.ResourceLocation

class RenderMermaid(renderManager: RenderManager)
    extends RenderLiving[EntityMermaid](renderManager, ModelMermaid, 0.5F) {
  override def getEntityTexture(entity: EntityMermaid): ResourceLocation = ???
}
