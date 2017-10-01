package net.katsstuff.spookyharvestmoon.client

import scala.reflect.ClassTag

import net.katsstuff.spookyharvestmoon.CommonProxy
import net.katsstuff.spookyharvestmoon.client.particle.{IGlowParticle, ParticleRenderer}
import net.katsstuff.spookyharvestmoon.client.render.{RenderJackOLantern, RenderLanternMan, RenderMermaid, RenderWillOTheWisp, RenderWitch}
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.entity.Entity
import net.minecraftforge.fml.client.registry.{IRenderFactory, RenderingRegistry}

object ClientProxy
class ClientProxy extends CommonProxy {

  val particleRenderer = new ParticleRenderer

  override def registerRenderers(): Unit = {
    registerEntityRenderer(new RenderJackOLantern(_))
    registerEntityRenderer(new RenderLanternMan(_))
    registerEntityRenderer(new RenderMermaid(_))
    registerEntityRenderer(new RenderWillOTheWisp(_))
    registerEntityRenderer(new RenderWitch(_))
  }

  def registerEntityRenderer[A <: Entity: ClassTag](f: RenderManager => Render[A]): Unit = {
    val factory: IRenderFactory[A] = manager => f(manager)
    RenderingRegistry.registerEntityRenderingHandler(
      implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]],
      factory
    )
  }

  override def addParticle(particle: IGlowParticle): Unit = {
    particleRenderer.addParticle(particle)
  }
}
