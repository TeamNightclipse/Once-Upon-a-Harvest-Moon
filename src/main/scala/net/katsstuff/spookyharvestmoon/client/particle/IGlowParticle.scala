package net.katsstuff.spookyharvestmoon.client.particle

import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.entity.Entity

trait IGlowParticle {
  //These methods need to delegate to the respective minecraft methods
  def onUpdateGlow(): Unit
  def renderParticleGlow(buffer: BufferBuilder, entityIn: Entity, partialTicks: Float, rotationX: Float,
      rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float): Unit

  def isAdditive: Boolean
  def ignoreDepth: Boolean
  def alive: Boolean
}