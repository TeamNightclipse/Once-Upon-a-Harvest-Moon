package net.katsstuff.spookyharvestmoon.entity

import net.katsstuff.spookyharvestmoon.{EggInfo, SpookyConfig}
import net.katsstuff.spookyharvestmoon.lib.LibEntityName
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.ai.EntityMoveHelper
import net.minecraft.entity.{EnumCreatureType, MoverType, SharedMonsterAttributes}
import net.minecraft.network.datasync.{DataSerializers, EntityDataManager}
import net.minecraft.pathfinding.{PathNavigate, PathNavigateSwimmer}
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraftforge.common.BiomeDictionary

object EntityMermaid {
  private val Moving = EntityDataManager.createKey(classOf[EntityMermaid], DataSerializers.BOOLEAN)
  implicit val info: EntityInfoConfig[EntityMermaid] = new EntityInfoConfig[EntityMermaid] {
    override def create(world: World): EntityMermaid   = new EntityMermaid(world)
    override def name:                 String          = LibEntityName.Mermaid
    override def egg:                  Option[EggInfo] = Some(EggInfo(0xFFFFFF, 0x000000))

    override def configEntry: SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.mermaid
    override def creatureType = EnumCreatureType.MONSTER
    override def biomes: Seq[Biome] = SpawnInfo.biomesForTypes(BiomeDictionary.Type.OCEAN)
  }
}
class EntityMermaid(_world: World) extends EntitySpookySpawnedMob(_world) {
  moveHelper = new SwimmingMoveHelper(this)

  override protected def createNavigator(world: World): PathNavigate = new PathNavigateSwimmer(this, world)

  override protected def entityInit(): Unit = {
    super.entityInit()
    dataManager.register(EntityMermaid.Moving, Boolean.box(false))
  }

  def moving:                    Boolean = this.dataManager.get(EntityMermaid.Moving)
  def moving_=(moving: Boolean): Unit    = dataManager.set(EntityMermaid.Moving, Boolean.box(moving))

  override def getBlockPathWeight(pos: BlockPos): Float =
    if (world.getBlockState(pos).getMaterial == Material.WATER) 10.0F + world.getLightBrightness(pos) - 0.5F
    else super.getBlockPathWeight(pos)

  override def travel(strafe: Float, vertical: Float, forward: Float): Unit = {
    if (isServerWorld && isInWater) {
      moveRelative(strafe, vertical, forward, 0.1F)
      move(MoverType.SELF, motionX, motionY, motionZ)
      motionX *= 0.9D
      motionY *= 0.9D
      motionZ *= 0.9D
      if (!moving) motionY -= 0.005D
    } else super.travel(strafe, vertical, forward)
  }

  override def spawnEntry: SpookyConfig.Spawns.SpawnEntry = SpookyConfig.spawns.mermaid

  override def spawnBlockCheck(state: IBlockState): Boolean = state.getMaterial == Material.WATER

  override def lootTableName: String = LibEntityName.Mermaid
}

class SwimmingMoveHelper(mermaid: EntityMermaid) extends EntityMoveHelper(mermaid) {
  override def onUpdateMoveHelper(): Unit = {
    if ((action == EntityMoveHelper.Action.MOVE_TO) && !mermaid.getNavigator.noPath) {
      val dx   = posX - mermaid.posX
      var dy   = posY - mermaid.posY
      val dz   = posZ - mermaid.posZ
      val dist = MathHelper.sqrt(dx * dx + dy * dy + dz * dz)

      dy = dy / dist
      val f = (MathHelper.atan2(dz, dx) * (180D / Math.PI)).toFloat - 90.0F

      mermaid.rotationYaw = limitAngle(mermaid.rotationYaw, f, 90.0F)
      mermaid.renderYawOffset = mermaid.rotationYaw
      val acceleration =
        (speed * mermaid.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue).toFloat
      mermaid.setAIMoveSpeed(mermaid.getAIMoveSpeed + (acceleration - mermaid.getAIMoveSpeed) * 0.125F)

      var d4 = Math.sin((mermaid.ticksExisted + mermaid.getEntityId) * 0.5D) * 0.05D
      val d5 = Math.cos(mermaid.rotationYaw * 0.017453292F)
      val d6 = Math.sin(mermaid.rotationYaw * 0.017453292F)

      mermaid.motionX += d4 * d5
      mermaid.motionZ += d4 * d6
      d4 = Math.sin((mermaid.ticksExisted + mermaid.getEntityId) * 0.75D) * 0.05D

      mermaid.motionY += d4 * (d6 + d5) * 0.25D
      mermaid.motionY += mermaid.getAIMoveSpeed * dy * 0.1D

      val d7 = mermaid.posX + dx / dist * 2.0D
      val d8 = mermaid.getEyeHeight + mermaid.posY + dy / dist
      val d9 = mermaid.posZ + dz / dist * 2.0D

      val lookHelper = mermaid.getLookHelper
      val (d10, d11, d12) = if (lookHelper.getIsLooking) {
        (lookHelper.getLookPosX, lookHelper.getLookPosY, lookHelper.getLookPosZ)
      } else {
        (d7, d8, d9)
      }
      mermaid.getLookHelper.setLookPosition(
        d10 + (d7 - d10) * 0.125D,
        d11 + (d8 - d11) * 0.125D,
        d12 + (d9 - d12) * 0.125D,
        10.0F,
        40.0F
      )
      mermaid.moving = true
    } else {
      mermaid.setAIMoveSpeed(0.0F)
      mermaid.moving = false
    }
  }
}
