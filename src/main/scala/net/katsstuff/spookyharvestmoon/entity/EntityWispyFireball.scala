package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.SpookyHarvestMoon
import net.katsstuff.spookyharvestmoon.client.particle.GlowTexture
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntitySmallFireball
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.lang.{Integer => JInt}

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.{DataParameter, DataSerializers, EntityDataManager}

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

  def this(world: World, x: Double, y: Double, z: Double, accelX: Double, accelY: Double, accelZ: Double, color: Int) {
    this(world)
    setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch)
    setPosition(x, y, z)
    val accel = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ).toDouble
    accelerationX = accelX / accel * 0.1D
    accelerationY = accelY / accel * 0.1D
    accelerationZ = accelZ / accel * 0.1D
    this.color = color
  }

  def this(world: World, shooter: EntityLivingBase, accelX: Double, accelY: Double, accelZ: Double, color: Int) {
    this(world)
    shootingEntity = shooter
    setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch)
    setPosition(posX, posY, posZ)
    motionX = 0.0D
    motionY = 0.0D
    motionZ = 0.0D
    val randAccelX = accelX + rand.nextGaussian() * 0.4D
    val randAccelY = accelY + rand.nextGaussian() * 0.4D
    val randAccelZ = accelZ + rand.nextGaussian() * 0.4D
    val accel      = MathHelper.sqrt(randAccelX * randAccelX + randAccelY * randAccelY + randAccelZ * randAccelZ).toDouble
    accelerationX = randAccelX / accel * 0.1D
    accelerationY = randAccelY / accel * 0.1D
    accelerationZ = randAccelZ / accel * 0.1D
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
    if (world.isRemote) {
      val size = 0.4F

      for (i <- 0 until 10) {
        val coeff = i / 2D
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
