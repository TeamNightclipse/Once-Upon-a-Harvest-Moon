package net.katsstuff.spookyharvestmoon.block

import java.util

import javax.annotation.Nullable

import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.entity.EntityWitch
import net.katsstuff.spookyharvestmoon.helper.ItemNBTHelper
import net.katsstuff.spookyharvestmoon.{LibBlockName, SpookyItems}
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.{PropertyBool, PropertyDirection}
import net.minecraft.block.state.{BlockFaceShape, BlockStateContainer, IBlockState}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumFacing, EnumHand, Mirror, NonNullList, Rotation}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object BlockTotem {
  val BrokenAABB         = new AxisAlignedBB(0.0625D * 4, 0.0625D * 0, 0.0625D * 4, 0.0625D * 12, 0.0625D * 10, 0.0625D * 12)
  val NormalAABB         = new AxisAlignedBB(0.0625D * 4, 0.0625D * 0, 0.0625D * 4, 0.0625D * 12, 0.0625D * 14, 0.0625D * 12)
  protected val TODOAABB = new AxisAlignedBB(0.4D, 0.0D, 0.4D, 0.6D, 0.6D, 0.6D)
  val Facing: PropertyDirection = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)
  val Broken: PropertyBool      = PropertyBool.create("broken")
}
class BlockTotem extends BlockSpookyBase(LibBlockName.Totem, Material.WOOD) {

  setDefaultState(
    blockState.getBaseState
      .withProperty(BlockTotem.Facing, EnumFacing.NORTH)
      .withProperty(BlockTotem.Broken, Boolean.box(false))
  )
  setSoundType(SoundType.WOOD)
  setHardness(1F)

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    if (state.getValue(BlockTotem.Broken)) BlockTotem.BrokenAABB
    else BlockTotem.NormalAABB

  @Nullable
  override def getCollisionBoundingBox(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    if (state.getValue(BlockTotem.Broken)) BlockTotem.BrokenAABB
    else BlockTotem.NormalAABB

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isFullCube(state: IBlockState): Boolean = false

  override def onBlockPlacedBy(
      world: World,
      pos: BlockPos,
      state: IBlockState,
      placer: EntityLivingBase,
      stack: ItemStack
  ): Unit = {
    super.onBlockPlacedBy(world, pos, state, placer, stack)

    if (ItemNBTHelper.getBoolean(stack, "Broken")) {
      world.setBlockState(pos, state.withProperty(BlockTotem.Broken, Boolean.box(true)))
    }
  }

  override def getSubBlocks(itemIn: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    val stack = new ItemStack(this)
    items.add(stack)

    val brokenStack = stack.copy()
    ItemNBTHelper.setBoolean(brokenStack, "Broken", b = true)
    items.add(brokenStack)
  }

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
    if (stack.getItem == SpookyItems.WispyFire) {
      if (!world.isRemote) {
        EntityWitch.validArena(world, pos) match {
          case Right(pillars) =>
            val witch = new EntityWitch(
              world,
              new Vector3(pos.getX + 0.5D, pos.getY + 3.5D, pos.getZ + 0.5D),
              pillars,
              state.getValue(BlockTotem.Broken)
            )
            world.spawnEntity(witch)
            true
          case Left((e, poses)) =>
            player.sendMessage(new TextComponentTranslation(e))
            if (poses.nonEmpty) {
              player.sendMessage(
                new TextComponentString(poses.take(10).map(p => s"x=${p.getX} y=${p.getY} z=${p.getZ}").mkString("\n"))
              )
            }
            false
        }
      } else true
    } else false
  }

  override def canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean =
    worldIn.getBlockState(pos).getBlock.isReplaceable(worldIn, pos) &&
      worldIn.getBlockState(pos.down).isSideSolid(worldIn, pos, EnumFacing.UP)

  override def getStateForPlacement(
      worldIn: World,
      pos: BlockPos,
      facing: EnumFacing,
      hitX: Float,
      hitY: Float,
      hitZ: Float,
      meta: Int,
      placer: EntityLivingBase
  ): IBlockState = getDefaultState.withProperty(BlockTotem.Facing, placer.getHorizontalFacing)

  override def getStateFromMeta(meta: Int): IBlockState =
    getDefaultState
      .withProperty(BlockTotem.Facing, EnumFacing.getHorizontal(meta))
      .withProperty(BlockTotem.Broken, Boolean.box((meta & 4) > 0))

  override def getMetaFromState(state: IBlockState): Int = {
    val i = state.getValue(BlockTotem.Facing).getHorizontalIndex

    if (state.getValue(BlockTotem.Broken)) i | 4
    else i
  }

  override def withRotation(state: IBlockState, rot: Rotation): IBlockState =
    state.withProperty(BlockTotem.Facing, rot.rotate(state.getValue(BlockTotem.Facing)))

  override def withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState =
    state.withRotation(mirrorIn.toRotation(state.getValue(BlockTotem.Facing)))

  override protected def createBlockState: BlockStateContainer =
    new BlockStateContainer(this, BlockTotem.Facing, BlockTotem.Broken)

  override def getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
    BlockFaceShape.UNDEFINED
}
