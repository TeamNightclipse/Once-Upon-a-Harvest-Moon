package net.katsstuff.spookyharvestmoon.entity

import java.lang.{Byte => JByte}

import scala.util.Random

import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, ParticleUtil}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.katsstuff.spookyharvestmoon.{SpookyConfig, SpookyHarvestMoon}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.ai._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntitySmallFireball
import net.minecraft.entity.{EnumCreatureAttribute, IEntityLivingData, SharedMonsterAttributes}
import net.minecraft.init.SoundEvents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.{DataParameter, DataSerializers, EntityDataManager}
import net.minecraft.pathfinding.PathNodeType
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.util.{DamageSource, SoundEvent}
import net.minecraft.world.{DifficultyInstance, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object EntityWillOTheWisp {
  var counter = 0
  def nextCounter(): Byte = {
    if (counter == 3) counter = 0
    else counter += 1

    counter.toByte
  }

  def formToColor(form: Byte): Int = {
    form match {
      case 0 => 0x802828
      case 1 => 0x282860
      case 2 => 0xFF8018
      case _ => 0xFFFFFF
    }
  }

  private final val Form: DataParameter[JByte] =
    EntityDataManager.createKey(classOf[EntityWillOTheWisp], DataSerializers.BYTE)
}
class EntityWillOTheWisp(_world: World) extends EntityFlyingMob(_world) {
  setSize(0.5F, 0.5F)

  form = {
    if (world.isRemote) 0
    else EntityWillOTheWisp.nextCounter()
  }

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

  override def onInitialSpawn(difficulty: DifficultyInstance, livingData: IEntityLivingData): IEntityLivingData = {
    val superData = super.onInitialSpawn(difficulty, livingData)

    val pos = findClosestLake.getOrElse(getPosition)

    if (world.isAirBlock(pos.up(2))) {
      setPositionAndUpdate(pos.getX, pos.getY + 2, pos.getZ)
    } else if (world.isAirBlock(pos.up(1))) {
      setPositionAndUpdate(pos.getX, pos.getY + 1, pos.getZ)
    }

    superData
  }

  private def findClosestLake: Option[BlockPos] = {
    val bb = getEntityBoundingBox.grow(32D, 8D, 32D)

    val xMin            = MathHelper.floor(bb.minX)
    val xMax            = MathHelper.ceil(bb.maxX)
    val yMin            = MathHelper.floor(bb.minY)
    val yMax            = MathHelper.ceil(bb.maxY)
    val zMin            = MathHelper.floor(bb.minZ)
    val zMax            = MathHelper.ceil(bb.maxZ)
    val mutableBlockPos = BlockPos.PooledMutableBlockPos.retain()

    val waterBlocks = for {
      x <- xMin until xMax
      y <- yMin until yMax
      z <- zMin until zMax
      if world.getBlockState(mutableBlockPos.setPos(x, y, z)).getMaterial == Material.WATER
    } yield new BlockPos(x, y, z)

    val posToDepth    = waterBlocks.groupBy(v => (v.getX, v.getZ)).mapValues(_.length)
    val averageDepth  = Math.round(posToDepth.values.sum.toDouble / posToDepth.size)
    val filteredPoses = posToDepth.filter(_._2 >= averageDepth)
    val applicablePoses = filteredPoses.keys.flatMap {
      case (x, z) =>
        val isSurrounded = (x - 2 to x + 2).forall(
          xTest => (z - 2 to z + 2).forall(zTest => filteredPoses.get((xTest, zTest)).exists(_ >= averageDepth))
        )
        if (!isSurrounded) None
        else {
          val topPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z))
          if (world.getBlockState(topPos).getMaterial == Material.WATER) Some(topPos) else None
        }
    }

    Random.shuffle(applicablePoses).headOption
  }

  override protected def applyEntityAttributes(): Unit = {
    super.applyEntityAttributes()
    getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6D)
    getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D)
  }

  override def getTalkInterval:                                      Int        = 30
  override protected def getAmbientSound:                            SoundEvent = SoundEvents.BLOCK_FIRE_AMBIENT
  override protected def getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE
  override protected def getDeathSound:                              SoundEvent = SoundEvents.BLOCK_FIRE_EXTINGUISH

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
    } else {
      if (world.isDaytime && isEntityAlive) {
        setDead()

        val basePos = pos
        val color   = EntityWillOTheWisp.formToColor(form)
        val r       = (color >> 16 & 255) / 255.0F
        val g       = (color >> 8 & 255) / 255.0F
        val b       = (color & 255) / 255.0F
        val size    = 0.4F
        for (i <- 0 until 32) {
          val motion = Vector3.randomDirection
          val pos    = basePos.offset(motion, Math.random())
          ParticleUtil.spawnParticleGlowPacket(world, pos, motion / 10D, r, g, b, size * 10F, 40, GlowTexture.Mote, 32)
        }
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
    val spawnMaterial = Seq(Material.GRASS, Material.GROUND, Material.ROCK, Material.WATER)
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
        wisp.attackEntityAsMob(target)

        val basePos = wisp.pos
        val color   = EntityWillOTheWisp.formToColor(wisp.form)
        val r       = (color >> 16 & 255) / 255.0F
        val g       = (color >> 8 & 255) / 255.0F
        val b       = (color & 255) / 255.0F
        val size    = 0.4F
        val towardsTarget = Vector3.directionToEntity(wisp, target)
        for (i <- 0 until 32) {
          val motion = Vector3.limitRandomDirection(towardsTarget, 20F)
          val pos    = basePos.offset(motion, Math.random())
          ParticleUtil.spawnParticleGlowPacket(wisp.world, pos, motion / 10D, r, g, b, size * 10F, 40, GlowTexture.Mote, 32)
        }
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
