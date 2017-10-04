package net.katsstuff.spookyharvestmoon.client

import scala.reflect.ClassTag

import net.katsstuff.spookyharvestmoon.CommonProxy
import net.katsstuff.spookyharvestmoon.client.helper.RenderHelper
import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, IGlowParticle, ParticleRenderer, ParticleUtil}
import net.katsstuff.spookyharvestmoon.client.render.{RenderJackOLantern, RenderLanternMan, RenderMermaid, RenderWillOTheWisp, RenderWitch}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.item.ItemNote
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.{ModelResourceLocation => MRL}
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.{IRenderFactory, RenderingRegistry}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ClientProxy {

  @SubscribeEvent
  def registerModels(event: ModelRegistryEvent): Unit = {
    import net.katsstuff.spookyharvestmoon.SpookyBlocks._
    import net.katsstuff.spookyharvestmoon.SpookyItems._

    registerItemBlock(Lantern)
    registerItemBlock(Hook)
    registerItemBlock(JackOLantern)
    ItemNote.Ids.foreach(i => registerItem(Note, i, i => new ResourceLocation(i.getRegistryName + s"_$i")))
    registerItem(WispyFire)
  }

  private def registerItemBlock(block: Block, damage: Int = 0): Unit =
    registerItem(Item.getItemFromBlock(block), damage)

  def registerItem(item: Item, damage: Int = 0, mrlFun: Item => ResourceLocation = _.getRegistryName): Unit =
    ModelLoader.setCustomModelResourceLocation(item, damage, new MRL(mrlFun(item), "inventory"))
}
class ClientProxy extends CommonProxy {

  val particleRenderer = new ParticleRenderer

  override def registerRenderers(): Unit = {
    registerEntityRenderer(new RenderJackOLantern(_))
    registerEntityRenderer(new RenderLanternMan(_))
    registerEntityRenderer(new RenderMermaid(_))
    registerEntityRenderer(new RenderWillOTheWisp(_))
    registerEntityRenderer(new RenderWitch(_))
  }

  override def bakeRenderModels(): Unit =
    RenderHelper.bakeModels()

  def registerEntityRenderer[A <: Entity: ClassTag](f: RenderManager => Render[A]): Unit = {
    val factory: IRenderFactory[A] = manager => f(manager)
    RenderingRegistry.registerEntityRenderingHandler(
      implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]],
      factory
    )

    MinecraftForge.EVENT_BUS.register(particleRenderer)
  }

  override def spawnParticleGlow(
      world: World,
      pos: Vector3,
      motion: Vector3,
      r: Float,
      g: Float,
      b: Float,
      scale: Float,
      lifetime: Int,
      texture: GlowTexture
  ): Unit = ParticleUtil.spawnParticleGlow(world, pos, motion, r, g, b, scale, lifetime, texture)

  override def addParticle(particle: IGlowParticle): Unit =
    particleRenderer.addParticle(particle)
}
