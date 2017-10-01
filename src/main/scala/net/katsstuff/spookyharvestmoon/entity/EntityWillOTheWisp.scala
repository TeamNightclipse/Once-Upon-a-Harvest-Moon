package net.katsstuff.spookyharvestmoon.entity

import java.lang.{Byte => JByte}

import net.katsstuff.spookyharvestmoon.client.particle.GlowTexture
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.katsstuff.spookyharvestmoon.{SpookyConfig, SpookyHarvestMoon}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.ai.{EntityAIBase, EntityAIFleeSun, EntityAIHurtByTarget, EntityAILookIdle, EntityAIMoveTowardsRestriction, EntityAINearestAttackableTarget, EntityAIRestrictSun, EntityAIWanderAvoidWater, EntityAIWatchClosest}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntitySmallFireball
import net.minecraft.entity.{EnumCreatureAttribute, SharedMonsterAttributes}
import net.minecraft.init.SoundEvents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.{DataParameter, DataSerializers, EntityDataManager}
import net.minecraft.pathfinding.PathNodeType
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.util.{DamageSource, SoundEvent}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object EntityWillOTheWisp {
  def formToColor(form: Byte): Int =
    form match {
      case 0 => 0xA0A0A0
      case 1 => 0x802828
      case 2 => 0xFF8018
      case 3 => 0xFFFF18
      case _ => 0xFFFFFF
    }

  private final val Form: DataParameter[JByte] =
    EntityDataManager.createKey(classOf[EntityWillOTheWisp], DataSerializers.BYTE)
}
class EntityWillOTheWisp(_world: World) extends EntityFlyingMob(_world) {

  setPathPriority(PathNodeType.WATER, -1.0F)
  setPathPriority(PathNodeType.DANGER_FIRE, 0.0F)
  setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F)
  isImmuneToFire = true

  override def initEntityAI(): Unit = {
    tasks.addTask(2, new EntityAIRestrictSun(this))
    tasks.addTask(3, new EntityAIFleeSun(this, 1.0D))
    tasks.addTask(4, new AIFireballAttack(this))
    tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1D))
    tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1D, 0F))
    tasks.addTask(8, new EntityAIWatchClosest(this, classOf[EntityPlayer], 8.0F))
    tasks.addTask(8, new EntityAILookIdle(this))
    targetTasks.addTask(1, new EntityAIHurtByTarget(this, true))
    targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, classOf[EntityPlayer], true))
  }

  override def entityInit(): Unit = {
    super.entityInit()
    dataManager.register(EntityWillOTheWisp.Form, Byte.box(0))
  }

  override protected def applyEntityAttributes(): Unit = {
    super.applyEntityAttributes()
    getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6D)
    getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D)
  }

  //TODO: Find out if these sounds are good enough (probably not for hurt and death)
  override protected def getAmbientSound:                            SoundEvent = SoundEvents.ENTITY_BLAZE_AMBIENT
  override protected def getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.ENTITY_BLAZE_HURT
  override protected def getDeathSound:                              SoundEvent = SoundEvents.ENTITY_BLAZE_DEATH

  @SideOnly(Side.CLIENT) override def getBrightnessForRender = 15728880
  override def getBrightness                                 = 1.0F

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
        SpookyHarvestMoon.proxy.spawnParticleGlow(world, pos, motion, r, g, b, size * 15F, 40, GlowTexture.Mote)
      }
    }
  }

  override protected def updateAITasks(): Unit = {
    if (isWet) attackEntityFrom(DamageSource.DROWN, 1.0F)
    super.updateAITasks()
  }

  override def lootTableName: String                         = LibEntityName.WillOTheWisp
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

class AIFireballAttack(wisp: EntityWillOTheWisp) extends EntityAIBase {
  setMutexBits(3)
  private var attackStep = 0
  private var attackTime = 0

  override def shouldExecute: Boolean = {
    val target = wisp.getAttackTarget
    target != null && target.isEntityAlive
  }

  override def startExecuting(): Unit =
    this.attackStep = 0

  override def updateTask(): Unit = {
    this.attackTime -= 1
    val target = wisp.getAttackTarget
    val dist2  = wisp.getDistanceSq(target)
    if (dist2 < 2 * 2) {
      if (attackTime <= 0) {
        attackTime = 20
        //TODO: Release fire particles
        wisp.attackEntityAsMob(target)
      }
      wisp.getMoveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1D)
    } else if (dist2 < getFollowDistance * getFollowDistance) {
      val dx = target.posX - wisp.posX
      val dy = target.getEntityBoundingBox.minY + (target.height / 2F) - (wisp.posY + (wisp.height / 2F))
      val dz = target.posZ - wisp.posZ
      if (attackTime <= 0) {
        attackStep += 1
        if (attackStep == 1) {
          attackTime = 60
        } else if (attackStep <= 4) attackTime = 6
        else {
          attackTime = 100
          attackStep = 0
        }
        if (attackStep > 1) {
          val distSq = MathHelper.sqrt(MathHelper.sqrt(dist2)) * 0.5F
          wisp.world.playEvent(null, 1018, new BlockPos(wisp.posX, wisp.posY, wisp.posZ), 0)

          //TODO: Replace with particle fireball
          val fireball = new EntitySmallFireball(
            wisp.world,
            wisp,
            dx + wisp.getRNG.nextGaussian * distSq,
            dy,
            dz + wisp.getRNG.nextGaussian * distSq
          )
          fireball.posY = wisp.posY + (wisp.height / 2.0F) + 0.5D
          wisp.world.spawnEntity(fireball)
        }
      }
      wisp.getLookHelper.setLookPositionWithEntity(target, 10.0F, 10.0F)
    } else {
      wisp.getNavigator.clearPath()
      wisp.getMoveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1D)
    }
    super.updateTask()
  }

  private def getFollowDistance = {
    val followRange = wisp.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
    if (followRange == null) 16.0D
    else followRange.getAttributeValue
  }
}
