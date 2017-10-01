package net.katsstuff.spookyharvestmoon.client.particle

import net.katsstuff.spookyharvestmoon.data.Vector3
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.entity.Entity
import net.minecraft.world.World

abstract class AbstractParticleGlow(_world: World, pos: Vector3, speed: Vector3)
    extends Particle(_world, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z)
    with IGlowParticle {

  override def onUpdateGlow(): Unit = super.onUpdate()
  override def renderParticleGlow(
      buffer: BufferBuilder,
      entityIn: Entity,
      partialTicks: Float,
      rotationX: Float,
      rotationZ: Float,
      rotationYZ: Float,
      rotationXY: Float,
      rotationXZ: Float
  ): Unit =
    super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ)
}
