package com.rikmuld.camping.objs.tile

import com.rikmuld.corerm.objs.RMTile
import com.rikmuld.camping.objs.entity.Mountable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fml.relauncher.SideOnly
import com.rikmuld.camping.objs.entity.Mountable
import net.minecraftforge.fml.relauncher.Side
import net.minecraft.world.World
import com.rikmuld.corerm.misc.WorldBlock._
import com.rikmuld.camping.Utils._
import net.minecraft.entity.player.EntityPlayer
import java.util.ArrayList
import scala.collection.JavaConversions._
import net.minecraft.util.ITickable

class TileLight extends RMTile with ITickable {
  var tick: Int = _

  override def update {
    if (!world.isRemote) {
      tick += 1
      if (tick > 10) {
        var flag = true
        val players = world.getEntitiesWithinAABB(classOf[EntityPlayer], new AxisAlignedBB(pos.getX - 2, pos.getY - 2, pos.getZ - 2, pos.getX + 2, pos.getY + 2, pos.getZ+ 2)).asInstanceOf[ArrayList[EntityPlayer]]
        for (player <- players if player.hasLantarn()) flag = false
        if (flag) (world, pos).toAir
        else tick = 0
      }
    }
  }
}