package net.katsstuff.spookyharvestmoon.block

import java.util.Random

import javax.annotation.Nullable

import scala.collection.JavaConverters._

import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, ParticleUtil}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.helper.LogHelper
import net.katsstuff.spookyharvestmoon.{LibBlockName, SpookyBlocks}
import net.minecraft.block.material.Material
import net.minecraft.block.properties.{PropertyBool, PropertyDirection}
import net.minecraft.block.state.{BlockFaceShape, BlockStateContainer, IBlockState}
import net.minecraft.block.{Block, SoundType}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, Vec3d}
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand, EnumParticleTypes, Mirror, NonNullList, Rotation}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object BlockLantern {
  protected val TODOAABB = new AxisAlignedBB(0.4D, 0.0D, 0.4D, 0.6D, 0.6D, 0.6D)
  val Facing: PropertyDirection = PropertyDirection.create("facing")
  val Light:  PropertyBool      = PropertyBool.create("light")
}
//A lot copied from BlockTorch
class BlockLantern extends BlockSpookyBase(LibBlockName.Lantern, Material.IRON) {

  setDefaultState(
    blockState.getBaseState
      .withProperty(BlockLantern.Facing, EnumFacing.UP)
      .withProperty(BlockLantern.Light, Boolean.box(true))
  )
  setTickRandomly(true)
  setSoundType(SoundType.METAL)
  setHardness(1F)

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = {
    state.getValue(BlockLantern.Facing) match {
      case EnumFacing.EAST  => BlockLantern.TODOAABB
      case EnumFacing.WEST  => BlockLantern.TODOAABB
      case EnumFacing.SOUTH => BlockLantern.TODOAABB
      case EnumFacing.NORTH => BlockLantern.TODOAABB
      case EnumFacing.DOWN  => new AxisAlignedBB(0.0625D * 4, 0D, 0.0625D * 5, 0.0625D * 12, 0.0625D * 12, 0.0625D * 11)
      case EnumFacing.UP    => new AxisAlignedBB(0.0625D * 4, 0D, 0.0625D * 5, 0.0625D * 12, 0.0625D * 12, 0.0625D * 11)
    }
  }

  @Nullable
  override def getCollisionBoundingBox(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB = {
    state.getValue(BlockLantern.Facing) match {
      case EnumFacing.EAST  => BlockLantern.TODOAABB
      case EnumFacing.WEST  => BlockLantern.TODOAABB
      case EnumFacing.SOUTH => BlockLantern.TODOAABB
      case EnumFacing.NORTH => BlockLantern.TODOAABB
      case EnumFacing.DOWN  => new AxisAlignedBB(0.0625D * 4, 0D, 0.0625D * 5, 0.0625D * 12, 0.0625D * 12, 0.0625D * 11)
      case EnumFacing.UP    => new AxisAlignedBB(0.0625D * 4, 0D, 0.0625D * 5, 0.0625D * 12, 0.0625D * 12, 0.0625D * 11)
    }
  }

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isFullCube(state: IBlockState): Boolean = false

  override def getLightValue(state: IBlockState, world: IBlockAccess, pos: BlockPos): Int =
    if (state.getValue(BlockLantern.Light)) 15 else 0

  private def canPlaceOn(worldIn: World, pos: BlockPos): Boolean = {
    val state = worldIn.getBlockState(pos)
    state.getBlock.canPlaceTorchOnTop(state, worldIn, pos)
  }

  override def canPlaceBlockAt(world: World, pos: BlockPos): Boolean =
    BlockLantern.Facing.getAllowedValues.asScala.exists(canPlaceAt(world, pos, _, existingBlock = false))

  private def canPlaceAt(world: World, pos: BlockPos, facing: EnumFacing, existingBlock: Boolean): Boolean = {
    val placeOn = pos.offset(facing.getOpposite)
    if (facing == EnumFacing.UP) {
      canPlaceOn(world, placeOn)
    } else {
      if (existingBlock) SpookyBlocks.Hook.canPlaceBlockAt(world, pos)
      else {
        val state = world.getBlockState(placeOn)
        val block = state.getBlock
        block == SpookyBlocks.Hook
      }
    }
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
  ): IBlockState = {
    if (worldIn.getBlockState(pos).getBlock == SpookyBlocks.Hook)
      getDefaultState.withProperty(BlockLantern.Facing, facing)
    else if (canPlaceAt(worldIn, pos, facing, existingBlock = false))
      getDefaultState.withProperty(BlockLantern.Facing, facing)
    else {
      EnumFacing.Plane.HORIZONTAL.asScala
        .find(canPlaceAt(worldIn, pos, _, existingBlock = false))
        .map(getDefaultState.withProperty(BlockLantern.Facing, _))
        .getOrElse(getDefaultState)
    }
  }

  override def onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState): Unit = checkAndDrop(worldIn, pos, state)

  override def neighborChanged(
      state: IBlockState,
      worldIn: World,
      pos: BlockPos,
      blockIn: Block,
      fromPos: BlockPos
  ): Unit = onNeighborChangeInternal(worldIn, pos, state)

  private def onNeighborChangeInternal(worldIn: World, pos: BlockPos, state: IBlockState): Boolean =
    if (!checkAndDrop(worldIn, pos, state)) true
    else {
      val facing   = state.getValue(BlockLantern.Facing)
      val axis     = facing.getAxis
      val opposite = facing.getOpposite
      val placedOn = pos.offset(opposite)
      val shouldDrop =
        if (facing != EnumFacing.DOWN)
          worldIn.getBlockState(placedOn).getBlockFaceShape(worldIn, placedOn, facing) != BlockFaceShape.SOLID
        else !canPlaceOn(worldIn, placedOn)

      if (shouldDrop) {
        dropBlockAsItem(worldIn, pos, state, 0)
        worldIn.setBlockToAir(pos)
        true
      } else false
    }

  private def checkAndDrop(worldIn: World, pos: BlockPos, state: IBlockState): Boolean =
    if ((state.getBlock == this) && canPlaceAt(worldIn, pos, state.getValue(BlockLantern.Facing), existingBlock = true))
      true
    else {
      if (worldIn.getBlockState(pos).getBlock == this) {
        dropBlockAsItem(worldIn, pos, state, 0)
        worldIn.setBlockToAir(pos)
      }
      false
    }

  override def getDrops(
      drops: NonNullList[ItemStack],
      world: IBlockAccess,
      pos: BlockPos,
      state: IBlockState,
      fortune: Int
  ): Unit = {
    drops.add(new ItemStack(SpookyBlocks.Hook))
    drops.add(new ItemStack(SpookyBlocks.Lantern))
  }

  override def onBlockActivated(
      world: World,
      pos: BlockPos,
      state: IBlockState,
      playerIn: EntityPlayer,
      hand: EnumHand,
      facing: EnumFacing,
      hitX: Float,
      hitY: Float,
      hitZ: Float
  ): Boolean = {
    world.setBlockState(pos, state.withProperty(BlockLantern.Light, Boolean.box(!state.getValue(BlockLantern.Light))))
    true
  }

  @SideOnly(Side.CLIENT)
  override def randomDisplayTick(state: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit = {
    if (state.getValue(BlockLantern.Light)) {
      val facing  = state.getValue(BlockLantern.Facing)
      val x       = pos.getX + 0.5D
      val y       = pos.getY + 0.3D
      val z       = pos.getZ + 0.5D
      val vOffset = 0.22D
      val hOffset = 0.27D
      if (facing.getAxis.isHorizontal) {
        val placedOn = facing.getOpposite
        val px       = x + hOffset * placedOn.getFrontOffsetX
        val py       = y + vOffset
        val pz       = z + hOffset * placedOn.getFrontOffsetX
        ParticleUtil.spawnParticleGlow(worldIn, Vector3(px, py, pz), Vector3(0D, 0.01D, 0D), 1F, 0.8F, 0F, 1.2F, 40, GlowTexture.Mote)
      } else {
        ParticleUtil.spawnParticleGlow(worldIn, Vector3(x, y, z), Vector3(0D, 0.01D, 0D), 1F, 0.8F, 0F, 1.2F, 40, GlowTexture.Mote)
      }
    }
  }

  def getFacing(meta: Int): EnumFacing = {
    val i = meta & 7
    if (i > 5) EnumFacing.UP
    else EnumFacing.getFront(i)
  }

  override def getStateFromMeta(meta: Int): IBlockState =
    getDefaultState
      .withProperty(BlockLantern.Facing, getFacing(meta))
      .withProperty(BlockLantern.Light, Boolean.box((meta & 8) > 0))

  @SideOnly(Side.CLIENT)
  override def getBlockLayer = BlockRenderLayer.TRANSLUCENT

  shouldRender

  override def getMetaFromState(state: IBlockState): Int = {
    val i = state.getValue(BlockLantern.Facing).getIndex

    if (state.getValue(BlockLantern.Light)) i | 8
    else i
  }

  override def withRotation(state: IBlockState, rot: Rotation): IBlockState =
    state.withProperty(BlockLantern.Facing, rot.rotate(state.getValue(BlockLantern.Facing)))

  override def withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState =
    state.withRotation(mirrorIn.toRotation(state.getValue(BlockLantern.Facing)))

  override protected def createBlockState: BlockStateContainer =
    new BlockStateContainer(this, BlockLantern.Facing, BlockLantern.Light)

  override def getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
    BlockFaceShape.UNDEFINED
}
