package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.katsstuff.spookyharvestmoon.{SpookyBlocks, SpookyConfig, SpookyEffect}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.ai._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{Entity, EntityLivingBase, EnumCreatureAttribute, SharedMonsterAttributes}
import net.minecraft.init.{Blocks, SoundEvents}
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{DamageSource, SoundEvent}
import net.minecraft.world.World

class EntityLanternMan(_world: World) extends EntitySpookySpawnedMob(_world) {
  setHeldItem(getActiveHand, new ItemStack(SpookyBlocks.Lantern))

  override protected def initEntityAI(): Unit = {
    tasks.addTask(0, new EntityAISwimming(this))
    tasks.addTask(2, new EntityAIAttackMelee(this, 1D, true))
    tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1D))
    tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1D))
    tasks.addTask(8, new EntityAIWatchClosest(this, classOf[EntityPlayer], 32F))
    tasks.addTask(8, new EntityAILookIdle(this))
    targetTasks.addTask(1, new EntityAIHurtByTarget(this, true))
    targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, classOf[EntityPlayer], true))
  }

  override protected def applyEntityAttributes(): Unit = {
    super.applyEntityAttributes()
    this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D)
    this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4D)
    this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3D)
  }

  override def onLivingUpdate(): Unit = {
    if (world.isDaytime && !world.isRemote && !isChild) {
      val f = getBrightness
      if (f > 0.5F && rand.nextFloat * 30.0F < (f - 0.4F) * 2.0F && world.canSeeSky(new BlockPos(posX, posY + getEyeHeight, posZ))) {
        //TODO: Spawn fire particles when entity disappears
        setDead()
      }
    }
    super.onLivingUpdate()
  }

  override def attackEntityAsMob(entity: Entity): Boolean = {
    val flag = super.attackEntityAsMob(entity)
    if (flag) {
      entity match {
        case living: EntityLivingBase => living.addPotionEffect(new PotionEffect(SpookyEffect.Drowning, 10, 1))
        case _ =>
      }
    }
    flag
  }

  //TODO: Find better sounds
  override protected def getAmbientSound: SoundEvent = SoundEvents.ENTITY_ZOMBIE_AMBIENT

  override protected def getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.ENTITY_ZOMBIE_HURT

  override protected def getDeathSound: SoundEvent = SoundEvents.ENTITY_ZOMBIE_DEATH

  protected def getStepSound: SoundEvent = SoundEvents.ENTITY_ZOMBIE_STEP

  override def getCreatureAttribute = EnumCreatureAttribute.UNDEAD

  override def spawnEntry: SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.lanternMan

  override def spawnBlockCheck(state: IBlockState): Boolean = state.getBlock == Blocks.GRASS

  override def lootTableName: String = LibEntityName.LanternMan
}
