package net.katsstuff.spookyharvestmoon.block

import javax.annotation.Nullable

import scala.collection.JavaConverters._

import com.google.common.base.Predicate

import net.katsstuff.spookyharvestmoon.{LibBlockName, SpookyBlocks}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.{BlockFaceShape, BlockStateContainer, IBlockState}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand, Mirror, Rotation}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object BlockHook {
  protected val TODOAABB = new AxisAlignedBB(0.3125D, 0.0D, 0.625D, 0.6875D, 0.625D, 1.0D)
  val Facing: PropertyDirection = {
    val pred: Predicate[EnumFacing] = dir => dir != EnumFacing.UP
    PropertyDirection.create("facing", pred)
  }
}
class BlockHook extends BlockSpookyBase(LibBlockName.Hook, Material.IRON) {
  setDefaultState(blockState.getBaseState.withProperty(BlockHook.Facing, EnumFacing.NORTH))
  setHardness(0.5F)

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    state.getValue(BlockHook.Facing) match {
      case EnumFacing.EAST  => BlockHook.TODOAABB
      case EnumFacing.WEST  => BlockHook.TODOAABB
      case EnumFacing.SOUTH => BlockHook.TODOAABB
      case EnumFacing.NORTH => BlockHook.TODOAABB
      case EnumFacing.DOWN  => BlockHook.TODOAABB
      case _                => BlockHook.TODOAABB
    }

  @Nullable
  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    Block.NULL_AABB

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isFullCube(state: IBlockState): Boolean = false

  override def canPlaceBlockOnSide(worldIn: World, pos: BlockPos, side: EnumFacing): Boolean = {
    val placedOn = pos.offset(side.getOpposite)
    val state    = worldIn.getBlockState(placedOn)
    val flag     = Block.isExceptBlockForAttachWithPiston(state.getBlock)
    !flag && side.getAxis.isHorizontal &&
    (state.getBlockFaceShape(worldIn, placedOn, side) == BlockFaceShape.SOLID)
  }

  override def canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean =
    BlockHook.Facing.getAllowedValues.asScala.exists(canPlaceBlockOnSide(worldIn, pos, _))

  override def onBlockActivated(
      world: World,
      pos: BlockPos,
      state: IBlockState,
      player: EntityPlayer,
      hand: EnumHand,
      facing: EnumFacing,
      hitX: Float,
      hitY: Float,
      hitZ: Float
  ): Boolean = {
    val stack = player.getHeldItem(hand)
    if (stack.getItem == Item.getItemFromBlock(SpookyBlocks.Lantern)) {
      world.setBlockState(
        pos,
        SpookyBlocks.Lantern.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, stack.getMetadata, player, hand)
      )
      true
    } else false
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
    if (facing != EnumFacing.UP) getDefaultState.withProperty(BlockHook.Facing, facing)
    else getDefaultState

  override def neighborChanged(
      state: IBlockState,
      worldIn: World,
      pos: BlockPos,
      blockIn: Block,
      fromPos: BlockPos
  ): Unit = {
    if (blockIn != this) {
      if (checkForDrop(worldIn, pos, state)) {
        val facing = state.getValue(BlockHook.Facing)
        if (!canPlaceBlockOnSide(worldIn, pos, facing)) {
          dropBlockAsItem(worldIn, pos, state, 0)
          worldIn.setBlockToAir(pos)
        }
      }
    }
  }

  private def checkForDrop(worldIn: World, pos: BlockPos, state: IBlockState) =
    if (!canPlaceBlockAt(worldIn, pos)) {
      dropBlockAsItem(worldIn, pos, state, 0)
      worldIn.setBlockToAir(pos)
      false
    } else true

  @SideOnly(Side.CLIENT)
  override def getBlockLayer = BlockRenderLayer.CUTOUT_MIPPED

  override def getStateFromMeta(meta: Int): IBlockState =
    getDefaultState.withProperty(BlockHook.Facing, EnumFacing.getFront(meta))

  override def getMetaFromState(state: IBlockState): Int = state.getValue(BlockHook.Facing).getIndex

  override def withRotation(state: IBlockState, rot: Rotation): IBlockState =
    state.withProperty(BlockHook.Facing, rot.rotate(state.getValue(BlockHook.Facing)))

  override def withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState =
    state.withRotation(mirrorIn.toRotation(state.getValue(BlockHook.Facing)))

  override protected def createBlockState = new BlockStateContainer(this, BlockHook.Facing)

  override def getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
    BlockFaceShape.UNDEFINED
}
