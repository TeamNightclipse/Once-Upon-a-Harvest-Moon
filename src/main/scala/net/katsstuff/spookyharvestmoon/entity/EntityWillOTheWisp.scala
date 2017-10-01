package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.SpookyConfig
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.ai.{EntityAIFleeSun, EntityAIHurtByTarget, EntityAILookIdle, EntityAINearestAttackableTarget, EntityAIRestrictSun, EntityAISwimming, EntityAIWander}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.datasync.{DataParameter, DataSerializers, EntityDataManager}
import net.minecraft.world.World
import java.lang.{Byte => JByte}

import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, ParticleUtil}
import net.minecraft.nbt.NBTTagCompound

object EntityWillOTheWisp {
  def formToColor(form: Byte): Int =
    form match {
      case 0 => 0xA0A0A0
      case 1 => 0x802828
      case 2 => 0xFF8018
      case 3 => 0xFFFF18
      case _ => 0xFFFFFF
    }

  private final val Form: DataParameter[JByte] = EntityDataManager.createKey(classOf[EntityWillOTheWisp], DataSerializers.BYTE)
}
class EntityWillOTheWisp(_world: World) extends EntityFlyingMob(_world) {

  override def initEntityAI(): Unit = {
    this.tasks.addTask(0, new EntityAISwimming(this))
    this.tasks.addTask(2, new EntityAIRestrictSun(this))
    this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D))
    this.tasks.addTask(4, new EntityAIMoveRanged(this, speed, 16F))
    this.tasks.addTask(6, new EntityAIWander(this, speed))
    this.tasks.addTask(7, new EntityAILookIdle(this))
    this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false))
    this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, classOf[EntityPlayer], true))
  }

  override def entityInit(): Unit = {
    super.entityInit()
    dataManager.register(EntityWillOTheWisp.Form, Byte.box(0))
  }

  override def onUpdate(): Unit = {
    super.onUpdate()
    if (world.isRemote) {
      val color = EntityWillOTheWisp.formToColor(form)
      val r     = (color >> 16 & 255) / 255.0F
      val g     = (color >> 8 & 255) / 255.0F
      val b     = (color & 255) / 255.0F
      val size  = 0.4F

      for (i <- 0 until 2) {
        val coeff = i / 2D
        val pos = Vector3(
          prevPosX + (posX - prevPosX) * coeff,
          0.2F + prevPosY + (posY - prevPosY) * coeff,
          prevPosZ + (posZ - prevPosZ) * coeff
        )
        val motion =
          Vector3(0.0125f * (rand.nextFloat - 0.5f), 0.075f * rand.nextFloat, 0.0125f * (rand.nextFloat - 0.5f))
        ParticleUtil.spawnParticleGlow(world, pos, motion, r, g, b, size * 15F, 40, GlowTexture.Mote)
      }
    }
  }

  override def lootTableName: String            = LibEntityName.WillOTheWisp
  override def spawnEntry:    SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.willOTheWisp
  override def spawnBlockCheck(state: IBlockState): Boolean = {
    val spawnMaterial = Seq(Material.GRASS, Material.GROUND, Material.ROCK)
    spawnMaterial.contains(state.getMaterial)
  }

  override def getCreatureAttribute: EnumCreatureAttribute = EnumCreatureAttribute.UNDEAD

  def form:               Byte = dataManager.get(EntityWillOTheWisp.Form)
  def form_=(byte: Byte): Unit = dataManager.set(EntityWillOTheWisp.Form, Byte.box(byte))

  override def readEntityFromNBT(tag: NBTTagCompound): Unit = {
    super.readEntityFromNBT(tag)
    form = tag.getByte("form")
  }

  override def writeEntityToNBT(tag: NBTTagCompound): Unit = {
    super.writeEntityToNBT(tag)
    tag.setByte("form", form)
  }
}
