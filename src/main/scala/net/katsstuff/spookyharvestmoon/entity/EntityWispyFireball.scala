package net.katsstuff.spookyharvestmoon.entity

import java.lang.{Integer => JInt}

import net.katsstuff.spookyharvestmoon.SpookyHarvestMoon
import net.katsstuff.spookyharvestmoon.client.particle.GlowTexture
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntitySmallFireball
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.{DataParameter, DataSerializers, EntityDataManager}
import net.minecraft.util.DamageSource
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World

object EntityWispyFireball {
  implicit val info: EntityInfo[EntityWispyFireball] = new EntityInfo[EntityWispyFireball] {
    override def name:                 String              = LibEntityName.WispyFireball
    override def create(world: World): EntityWispyFireball = new EntityWispyFireball(world)
  }
  private final val Color: DataParameter[JInt] =
    EntityDataManager.createKey(classOf[EntityWispyFireball], DataSerializers.VARINT)
}
class EntityWispyFireball(_world: World) extends EntitySmallFireball(_world) {
  color = {
    if (world.isRemote) 0
    else EntityWillOTheWisp.nextCounter()
  }

  private def r = (color >> 16 & 255) / 255.0F
  private def g = (color >> 8 & 255) / 255.0F
  private def b = (color & 255) / 255.0F

  def this(world: World, pos: Vector3, direction: Vector3, speed: Double, color: Int) {
    this(world)
    setLocationAndAngles(pos.x, pos.y, pos.z, this.rotationYaw, this.rotationPitch)
    setPosition(pos.x, pos.y, pos.z)
    accelerationX = direction.normalize.x * speed
    accelerationY = direction.normalize.y * speed
    accelerationZ = direction.normalize.z * speed
    this.color = color
  }

  def this(world: World, shooter: EntityLivingBase, direction: Vector3, speed: Double, inaccuracy: Double, color: Int) {
    this(world)
    shootingEntity = shooter
    setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch)
    setPosition(posX, posY, posZ)
    motionX = 0.0D
    motionY = 0.0D
    motionZ = 0.0D
    val randDir = direction.add(rand.nextGaussian() * inaccuracy, rand.nextGaussian() * inaccuracy, rand.nextGaussian() * inaccuracy)
    accelerationX = randDir.normalize.x * speed
    accelerationY = randDir.normalize.y * speed
    accelerationZ = randDir.normalize.z * speed
    this.color = color
  }

  override def entityInit(): Unit = {
    super.entityInit()
    dataManager.register(EntityWispyFireball.Color, Int.box(0))
  }

  def color:               Int  = dataManager.get(EntityWispyFireball.Color)
  def color_=(color: Int): Unit = dataManager.set(EntityWispyFireball.Color, Int.box(color))

  override def onUpdate(): Unit = {
    super.onUpdate()
    if (world.isRemote && SpookyHarvestMoon.proxy.isInRenderRange(this)) {
      val size = 0.4F

      for (i <- 0 until 3) {
        val coeff = i / 3D
        val pos = Vector3(
          prevPosX + (posX - prevPosX) * coeff,
          0.2F + prevPosY + (posY - prevPosY) * coeff,
          prevPosZ + (posZ - prevPosZ) * coeff
        )
        val motion =
          Vector3(0.0125f * (rand.nextFloat - 0.5F), 0.075F * rand.nextFloat, 0.01F * (rand.nextFloat - 0.5F))
        SpookyHarvestMoon.proxy.spawnParticleGlow(world, pos, motion, r, g, b, size * 5F, 40, GlowTexture.Mote)
      }
    }
  }

  override def onImpact(result: RayTraceResult): Unit = {
    if (!world.isRemote) {
      if (result.entityHit != null) {
        if (!result.entityHit.isImmuneToFire) {
          val success = result.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), 5F)
          if (success) {
            applyEnchantments(shootingEntity, result.entityHit)
            result.entityHit.setFire(5)
          }
        }
      }
    }
  }

  override def isFireballFiery: Boolean = false

  override def readEntityFromNBT(tag: NBTTagCompound): Unit = {
    super.readEntityFromNBT(tag)
    color = tag.getInteger("color")
  }

  override def writeEntityToNBT(tag: NBTTagCompound): Unit = {
    super.writeEntityToNBT(tag)
    tag.setInteger("color", color)
  }
}
