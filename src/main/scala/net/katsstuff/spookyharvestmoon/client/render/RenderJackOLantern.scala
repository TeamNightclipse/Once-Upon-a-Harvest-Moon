package net.katsstuff.spookyharvestmoon.client.render

import net.katsstuff.spookyharvestmoon.client.model.ModelJackOLantern
import net.katsstuff.spookyharvestmoon.entity.EntityJackOLantern
import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.minecraft.client.renderer.entity.{RenderLiving, RenderManager}
import net.minecraft.util.ResourceLocation

class RenderJackOLantern(renderManager: RenderManager)
    extends RenderLiving[EntityJackOLantern](renderManager, ModelJackOLantern, 0.5F) {
  override def getEntityTexture(entity: EntityJackOLantern): ResourceLocation = new ResourceLocation(LibMod.Id, "textures/todo.png")
}
