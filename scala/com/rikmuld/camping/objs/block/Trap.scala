package com.rikmuld.camping.objs.block

import com.rikmuld.camping.objs.tile.TileTrap
import com.rikmuld.corerm.objs.ObjInfo
import com.rikmuld.corerm.objs.blocks.{RMBlockContainer, WithInstable, WithModel}
import com.rikmuld.corerm.tileentity.TileEntitySimple
import com.rikmuld.corerm.utils.WorldBlock._
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}

class Trap(modId:String, info:ObjInfo) extends RMBlockContainer(modId, info) with WithInstable with WithModel {
  setDefaultState(getStateFromMeta(0))

  override def getCollisionBoundingBox(state:IBlockState, world: IBlockAccess, pos:BlockPos): AxisAlignedBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0)
  override def getBoundingBox(state:IBlockState, source:IBlockAccess, pos:BlockPos):AxisAlignedBB = {
    Option(source.getTileEntity(pos)).map { tile =>
      if(tile.asInstanceOf[TileTrap].open)new AxisAlignedBB(0.21875f, 0, 0.21875f, 0.78125f, 0.1875f, 0.78125f)
      else new AxisAlignedBB(0.21875f, 0, 0.34375f, 0.78125f, 0.25f, 0.65f)
    }.getOrElse(new AxisAlignedBB(0, 0, 0, 0, 0, 0))
  }
  override def createNewTileEntity(world: World, meta: Int): TileEntitySimple = new TileTrap
  override def onBlockActivated(world: World, pos:BlockPos, state:IBlockState, player: EntityPlayer, hand:EnumHand, side: EnumFacing, xHit: Float, yHit: Float, zHit: Float): Boolean = {
    val tile = (world, pos).tile.asInstanceOf[TileTrap]
    tile.lastPlayer = Some(player)
    if (!world.isRemote && !world.getTileEntity(pos).asInstanceOf[TileTrap].open) {
      tile.forceOpen
      true
    } else super.onBlockActivated(world, pos, state, player, hand, side, xHit, yHit, zHit)
  }
}