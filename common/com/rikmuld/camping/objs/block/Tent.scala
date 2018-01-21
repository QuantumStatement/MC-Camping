package com.rikmuld.camping.objs.block

import com.rikmuld.corerm.objs.RMBlockContainer
import java.io.ObjectInput

import com.rikmuld.corerm.objs.ObjInfo
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.util.{EnumFacing, EnumHand, NonNullList}
import com.google.common.base.Predicate
import com.rikmuld.corerm.objs.WithProperties
import com.rikmuld.corerm.objs.WithModel
import com.rikmuld.corerm.objs.RMFacingHorizontalProp
import com.rikmuld.camping.objs.block.Tent._
import com.rikmuld.corerm.objs.WithInstable
import com.rikmuld.camping.objs.tile.TileEntityTent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.World
import net.minecraft.entity.Entity
import com.rikmuld.camping.objs.tile.TileEntityTent
import com.rikmuld.corerm.misc.WorldBlock._
import com.rikmuld.corerm.CoreUtils._
import com.rikmuld.camping.objs.tile.TileTent
import net.minecraft.dispenser.IBlockSource
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import java.util.ArrayList
import java.util.Random

import net.minecraft.item.ItemStack
import com.rikmuld.camping.objs.Objs
import net.minecraft.util.math.MathHelper
import com.rikmuld.corerm.objs.RMTile
import net.minecraft.world.IBlockAccess
import net.minecraft.entity.EntityLivingBase
import com.rikmuld.camping.objs.tile.TileEntityTent
import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConversions._
import net.minecraft.entity.player.EntityPlayer
import com.rikmuld.camping.objs.tile.TileEntityTent
import com.rikmuld.corerm.network.PacketSender
import com.rikmuld.camping.objs.misc.OpenGui
import net.minecraft.init.Items
import com.rikmuld.camping.objs.tile.TileEntityTent
import com.rikmuld.camping.objs.misc.OpenGui
import com.rikmuld.camping.objs.tile.TileTent
import com.rikmuld.camping.objs.tile.TileTent
import com.rikmuld.corerm.bounds.BoundsTracker
import com.rikmuld.camping.objs.BlockDefinitions
import com.rikmuld.corerm.objs.RMItemBlock
import com.rikmuld.camping.CampingMod
import net.minecraft.block.Block
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.fml.relauncher.Side
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item

import scala.collection.JavaConversions._

object Tent {
  val FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL.asInstanceOf[Predicate[EnumFacing]])
}

class Tent(modId:String, info:ObjInfo) extends RMBlockContainer(modId, info) with WithModel with WithProperties with WithInstable {
  setDefaultState(getStateFromMeta(0))
    
  var facingFlag:Int = _
  
  override def getProps = Array(new RMFacingHorizontalProp(FACING, 0))
  override def getCollisionBoundingBox(state:IBlockState, source:IBlockAccess, pos:BlockPos):AxisAlignedBB = {
    TileEntityTent.bounds(getFacing(state)).getBlockCollision
  }
  def getFacing(state:IBlockState) = state.getValue(Tent.FACING).asInstanceOf[EnumFacing].getHorizontalIndex
  override def breakBlock(world: World, pos:BlockPos, state:IBlockState) {
    val tileFlag = Option((world, pos).tile)
    if (tileFlag.isDefined&&tileFlag.get.isInstanceOf[TileTent]) {
      var tile = tileFlag.get.asInstanceOf[TileTent]
      if(tile.structures!=null&&tile.structures(getFacing(state))!=null)tile.structures(getFacing(state)).destroyStructure(world, tile.tracker(getFacing(state)))
      if (!world.isRemote && !tile.dropped) {
        tile.dropped = true
        val stacks = new ArrayList[ItemStack]
        dropBlockAsItem(world, pos, state, 1)
        stacks.addAll(tile.getContends)
        val stack = nwsk(this, tile.color)
        stacks.add(stack)
        world.dropItemsInWorld(stacks, pos.getX, pos.getY, pos.getZ, new Random())
      }
      super.breakBlock(world, pos, state)
    }
  }
  override def canPlaceBlockAt(world: World, pos:BlockPos): Boolean = {
    val bd = (world, pos)
    ((bd.block == null) || bd.isReplaceable) && Objs.tentStructure(facingFlag).canBePlaced(world, new BoundsTracker(bd.x, bd.y, bd.z, TileEntityTent.bounds(facingFlag)))
  }
  override def createNewTileEntity(world: World, meta: Int): RMTile = new TileTent
  override def onBlockPlacedBy(world: World, pos:BlockPos, state:IBlockState, entityLiving: EntityLivingBase, itemStack: ItemStack) {
    (world, pos).tile.asInstanceOf[TileTent].setColor(if (itemStack.hasTagCompound()) itemStack.getTagCompound.getInteger("color") else 15)
    (world, pos).setState(state.withProperty(Tent.FACING, entityLiving.facing))
    (world, pos).tile.asInstanceOf[TileTent].createStructure
  }
  override def quantityDropped(random: Random): Int = 0
  override def getBoundingBox(state:IBlockState, source:IBlockAccess, pos:BlockPos):AxisAlignedBB = {
    val tile = source.getTileEntity(pos).asInstanceOf[TileTent]
    TileEntityTent.bounds(getFacing(state)).getBlockBounds
  }
  override def dropIfCantStay(bd:BlockData) {
    val tile = bd.tile.asInstanceOf[TileTent]
    if (Option(tile.structures).isDefined && !tile.structures(getFacing(bd.state)).hadSolidUnderGround(bd.world, tile.tracker(getFacing(bd.state)))) {
      breakBlock(bd.world, bd.pos, bd.state)
    }
  }
  override def getLightValue(state:IBlockState, world: IBlockAccess, pos:BlockPos): Int = {
    val tile = world.getTileEntity(pos).asInstanceOf[TileTent]
    if (Option(tile).isDefined && (tile.lanternDamage == BlockDefinitions.Lantern.ON) && (tile.lanterns > 0)) 15 else 0
  }
  override def onBlockActivated(world: World, pos:BlockPos, state:IBlockState, player: EntityPlayer, hand:EnumHand, side: EnumFacing, xHit: Float, yHit: Float, zHit: Float): Boolean = {
    if (!world.isRemote) {
      val bd = (world, pos)
      val stack = player.getHeldItem(hand)
      val tile = bd.tile.asInstanceOf[TileTent]
      if ((stack != null) && tile.addContends(stack)) {
        stack.setCount(stack.getCount - 1)
        if(tile.lanterns == 1 && tile.chests == 2 && tile.beds == 1) player.addStat(Objs.achLuxury)
        if (stack.getCount < 0) player.setCurrentItem(null)
        return true
      } else if ((stack != null) && (stack.getItem() == Items.DYE) && (bd.tile.asInstanceOf[TileTent].color != stack.getItemDamage)) {
        bd.tile.asInstanceOf[TileTent].setColor(stack.getItemDamage)
        stack.setCount(stack.getCount - 1)
        return true
      } else super.onBlockActivated(world, pos, state, player, hand, side, xHit, yHit, zHit)
    }
    true
  }
}

class TentItem(block:Block) extends RMItemBlock(CampingMod.MOD_ID, BlockDefinitions.TENT, block) {  
  @SideOnly(Side.CLIENT)
  override def getSubItems(itemIn:Item, tab:CreativeTabs, subItems:NonNullList[ItemStack]) {
    subItems.asInstanceOf[java.util.List[ItemStack]].add(new ItemStack(itemIn, 1, 15)) 
  }
  override def placeBlockAt(stack: ItemStack, player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, newState: IBlockState): Boolean = {
    if(super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)){
      (world, pos).tile.asInstanceOf[TileTent].color = stack.getItemDamage
      (world, pos).tile.asInstanceOf[TileTent].setColor(stack.getItemDamage)
      true
    } else false
  }
}