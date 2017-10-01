package net.katsstuff.spookyharvestmoon.entity

import net.minecraft.entity.{EntityLiving, EntityLivingBase, IRangedAttackMob, SharedMonsterAttributes}
import net.minecraft.entity.ai.{EntityAIAttackRanged, EntityAIHurtByTarget, EntityAILookIdle, EntityAINearestAttackableTarget, EntityAISwimming, EntityAIWanderAvoidWater, EntityAIWatchClosest}
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.pathfinding.PathNavigateGround
import net.minecraft.potion.PotionEffect
import net.minecraft.world.{BossInfo, BossInfoServer, World}

class EntityWitch(_world: World) extends EntitySpookyBaseMob(_world) with IRangedAttackMob {

  private val bossInfo = new BossInfoServer(getDisplayName, BossInfo.Color.RED,
    BossInfo.Overlay.NOTCHED_6).setDarkenSky(true).setCreateFog(true).asInstanceOf[BossInfoServer]

  setHealth(getMaxHealth)
  isImmuneToFire = true
  getNavigator.asInstanceOf[PathNavigateGround].setCanSwim(true)
  experienceValue = 50

  override protected def initEntityAI(): Unit = {
    this.tasks.addTask(0, new EntityAISwimming(this))
    this.tasks.addTask(1, new EntityAIAttackRanged(this, 1.0D, 40, 20.0F))
    this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D))
    this.tasks.addTask(6, new EntityAIWatchClosest(this, classOf[EntityPlayer], 8.0F))
    this.tasks.addTask(7, new EntityAILookIdle(this))
    this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false))
    this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, classOf[EntityLiving], false, false))
  }

  override protected def applyEntityAttributes(): Unit = {
    super.applyEntityAttributes()
    getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(400D)
    getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D)
    getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40D)
    getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2D)
  }

  override def setSwingingArms(swingingArms: Boolean): Unit = ???
  override def attackEntityWithRangedAttack(target: EntityLivingBase, distanceFactor: Float): Unit = ???

  override def readEntityFromNBT(compound: NBTTagCompound): Unit = {
    super.readEntityFromNBT(compound)
    if (hasCustomName) bossInfo.setName(getDisplayName)
  }

  override def setCustomNameTag(name: String): Unit = {
    super.setCustomNameTag(name)
    bossInfo.setName(getDisplayName)
  }

  override def updateAITasks(): Unit = {
    super.updateAITasks()
    bossInfo.setPercent(getHealth / getMaxHealth)
  }

  override def setInWeb(): Unit = {}

  override def addTrackingPlayer(player: EntityPlayerMP): Unit = {
    super.addTrackingPlayer(player)
    bossInfo.addPlayer(player)
  }

  override def removeTrackingPlayer(player: EntityPlayerMP): Unit = {
    super.removeTrackingPlayer(player)
    bossInfo.removePlayer(player)
  }

  override def fall(distance: Float, damageMultiplier: Float): Unit = {}

  override def addPotionEffect(effect: PotionEffect): Unit = {
    if(!effect.getPotion.isBadEffect) {
      super.addPotionEffect(effect)
    }
  }

  override def isNonBoss = false
}
