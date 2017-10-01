package net.katsstuff.spookyharvestmoon.block

import java.util.Random

import javax.annotation.Nullable

import net.katsstuff.spookyharvestmoon.LibBlockName
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.{BlockFaceShape, BlockStateContainer, IBlockState}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumParticleTypes, Mirror, Rotation}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import scala.collection.JavaConverters._

import com.google.common.base.Predicate

object BlockLantern {
  protected val StandingAABB     = new AxisAlignedBB(0.4D, 0.0D, 0.4D, 0.6D, 0.6D, 0.6D)
  protected val LanternNorthAABB = new AxisAlignedBB(0.35D, 0.2D, 0.7D, 0.65D, 0.8D, 1.0D)
  protected val LanternSouthAABB = new AxisAlignedBB(0.35D, 0.2D, 0.0D, 0.65D, 0.8D, 0.3D)
  protected val LanternWestAABB  = new AxisAlignedBB(0.7D, 0.2D, 0.35D, 1.0D, 0.8D, 0.64D)
  protected val LanternEastAABB  = new AxisAlignedBB(0.0D, 0.2D, 0.35D, 0.3D, 0.8D, 0.64D)
  val Facing: PropertyDirection = {
    val pred: Predicate[EnumFacing] = (input: EnumFacing) => input != EnumFacing.DOWN
    PropertyDirection.create("facing", pred)
  }
}
//A lot copied from BlockTorch
class BlockLantern extends BlockSpookyBase(LibBlockName.Lantern, Material.IRON) {

  setDefaultState(blockState.getBaseState.withProperty(BlockLantern.Facing, EnumFacing.UP))
  setTickRandomly(true)
  setLightLevel(1F)

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    state.getValue(BlockLantern.Facing) match {
      case EnumFacing.EAST  => BlockLantern.LanternEastAABB
      case EnumFacing.WEST  => BlockLantern.LanternWestAABB
      case EnumFacing.SOUTH => BlockLantern.LanternSouthAABB
      case EnumFacing.NORTH => BlockLantern.LanternNorthAABB
      case _                => BlockLantern.StandingAABB
    }

  @Nullable
  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    Block.NULL_AABB

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isFullCube(state: IBlockState): Boolean = false

  private def canPlaceOn(worldIn: World, pos: BlockPos): Boolean = {
    val state = worldIn.getBlockState(pos)
    state.getBlock.canPlaceTorchOnTop(state, worldIn, pos)
  }

  override def canPlaceBlockAt(world: World, pos: BlockPos): Boolean =
    BlockLantern.Facing.getAllowedValues.asScala.exists(canPlaceAt(world, pos, _))

  private def canPlaceAt(world: World, pos: BlockPos, facing: EnumFacing): Boolean = {
    val placeOn   = pos.offset(facing.getOpposite)
    val state     = world.getBlockState(placeOn)
    val block     = state.getBlock
    val faceShape = state.getBlockFaceShape(world, placeOn, facing)
    if (facing == EnumFacing.UP && canPlaceOn(world, placeOn)) true
    else if ((facing != EnumFacing.UP) && (facing != EnumFacing.DOWN))
      !Block.isExceptBlockForAttachWithPiston(block) && (faceShape == BlockFaceShape.SOLID)
    else false
  }

  override def getStateForPlacement(
      worldIn: World,
      pos: BlockPos,
      facing: EnumFacing,
      hitX: Float,
      hitY: Float,
      hitZ: Float,
      meta: Int,
      placer: EntityLivingBase
  ): IBlockState =
    if (canPlaceAt(worldIn, pos, facing)) getDefaultState.withProperty(BlockLantern.Facing, facing)
    else {
      EnumFacing.Plane.HORIZONTAL.asScala
        .find(canPlaceAt(worldIn, pos, _))
        .map(getDefaultState.withProperty(BlockLantern.Facing, _))
        .getOrElse(getDefaultState)
    }

  override def onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState): Unit = checkForDrop(worldIn, pos, state)

  override def neighborChanged(
      state: IBlockState,
      worldIn: World,
      pos: BlockPos,
      blockIn: Block,
      fromPos: BlockPos
  ): Unit = onNeighborChangeInternal(worldIn, pos, state)

  protected def onNeighborChangeInternal(worldIn: World, pos: BlockPos, state: IBlockState): Boolean =
    if (!checkForDrop(worldIn, pos, state)) true
    else {
      val facing   = state.getValue(BlockLantern.Facing)
      val axis     = facing.getAxis
      val opposite = facing.getOpposite
      val placedOn = pos.offset(opposite)
      val isDropped = (axis.isHorizontal &&
        (worldIn.getBlockState(placedOn).getBlockFaceShape(worldIn, placedOn, facing) != BlockFaceShape.SOLID)) ||
        (axis.isVertical && !canPlaceOn(worldIn, placedOn))
      if (isDropped) {
        dropBlockAsItem(worldIn, pos, state, 0)
        worldIn.setBlockToAir(pos)
        true
      } else false
    }

  protected def checkForDrop(worldIn: World, pos: BlockPos, state: IBlockState): Boolean =
    if ((state.getBlock == this) && this.canPlaceAt(worldIn, pos, state.getValue(BlockLantern.Facing))) true
    else {
      if (worldIn.getBlockState(pos).getBlock == this) {
        dropBlockAsItem(worldIn, pos, state, 0)
        worldIn.setBlockToAir(pos)
      }
      false
    }

  @SideOnly(Side.CLIENT)
  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit = {
    val facing  = stateIn.getValue(BlockLantern.Facing)
    val x       = pos.getX + 0.5D
    val y       = pos.getY + 0.7D
    val z       = pos.getZ + 0.5D
    val vOffset = 0.22D
    val hOffset = 0.27D
    //TODO: Replace with glow particles
    if (facing.getAxis.isHorizontal) {
      val placedOn = facing.getOpposite
      val px       = x + hOffset * placedOn.getFrontOffsetX
      val py       = y + vOffset
      val pz       = z + hOffset * placedOn.getFrontOffsetX
      worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, 0D, 0D, 0D)
      worldIn.spawnParticle(EnumParticleTypes.FLAME, px, py, pz, 0D, 0D, 0D)
    } else {
      worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D)
      worldIn.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D)
    }
  }

  override def getStateFromMeta(meta: Int): IBlockState = {
    val facing = BlockLantern.Facing
    val state  = getDefaultState
    meta match {
      case 1     => state.withProperty(facing, EnumFacing.EAST)
      case 2     => state.withProperty(facing, EnumFacing.WEST)
      case 3     => state.withProperty(facing, EnumFacing.SOUTH)
      case 4     => state.withProperty(facing, EnumFacing.NORTH)
      case 5 | _ => state.withProperty(facing, EnumFacing.UP)
    }
  }

  @SideOnly(Side.CLIENT) override def getBlockLayer = BlockRenderLayer.CUTOUT

  override def getMetaFromState(state: IBlockState): Int = {
    state.getValue(BlockLantern.Facing) match {
      case EnumFacing.EAST                     => 1
      case EnumFacing.WEST                     => 2
      case EnumFacing.SOUTH                    => 3
      case EnumFacing.NORTH                    => 4
      case EnumFacing.DOWN | EnumFacing.UP | _ => 5
    }
  }

  override def withRotation(state: IBlockState, rot: Rotation): IBlockState =
    state.withProperty(BlockLantern.Facing, rot.rotate(state.getValue(BlockLantern.Facing)))

  override def withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState =
    state.withRotation(mirrorIn.toRotation(state.getValue(BlockLantern.Facing)))

  override protected def createBlockState = new BlockStateContainer(this, BlockLantern.Facing)

  override def getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
    BlockFaceShape.UNDEFINED
}
