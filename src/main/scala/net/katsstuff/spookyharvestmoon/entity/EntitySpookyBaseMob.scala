package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.lib.LibMod
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.monster.EntityMob
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

abstract class EntitySpookyBaseMob(_world: World) extends EntityMob(_world) {

  def lootTableName: String

  override def getLootTable: ResourceLocation = new ResourceLocation(LibMod.Id, s"entities/$lootTableName")

  def maxHP: Float = getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue.toFloat
  protected def maxHP_=(hp: Float): Unit = {
    getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(hp)
  }

  def speed: Double = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue
  def speed_=(speed: Double): Unit = {
    getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed)
  }

  def pos: Vector3 = new Vector3(this)
}
