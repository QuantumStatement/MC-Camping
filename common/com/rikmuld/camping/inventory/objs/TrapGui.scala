package com.rikmuld.camping.inventory.objs

import com.rikmuld.corerm.inventory.RMContainerTile
import com.rikmuld.corerm.client.GuiContainerSimple
import com.rikmuld.corerm.inventory.SlotOnlyItems
import net.minecraft.inventory.IInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.init.{Blocks, Items}
import net.minecraft.inventory.Slot
import net.minecraft.block.Block
import com.rikmuld.camping.Lib._
import com.rikmuld.corerm.CoreUtils._

class GuiTrap(player: EntityPlayer, inv: IInventory) extends GuiContainerSimple(new ContainerTrap(player, inv)) {
  ySize = 120

  def getTexture: String = TextureInfo.GUI_TRAP
  def getName: String = ""
  def hasName: Boolean = false
}

class ContainerTrap(player: EntityPlayer, inv: IInventory) extends RMContainerTile(player, inv) {
  addSlotToContainer(new SlotOnlyItems(inv, 0, 80, 12))

  this.addSlots(player.inventory, 0, 1, 9, 8, 96)
  this.addSlots(player.inventory, 9, 3, 9, 8, 38)

  override def transferStackInSlot(player: EntityPlayer, slotNum: Int): ItemStack = {
    var itemstack: ItemStack = ItemStack.EMPTY
    val slot = inventorySlots.get(slotNum).asInstanceOf[Slot]
    if ((slot != null) && slot.getHasStack) {
      val itemstack1 = slot.getStack
      itemstack = itemstack1.copy
      if (slotNum < inv.getSizeInventory) {
        if (!mergeItemStack(itemstack1, inv.getSizeInventory, inventorySlots.size, false)) return ItemStack.EMPTY
      } else if (Block.getBlockFromItem(itemstack1.getItem()) != null || (!mergeItemStack(itemstack1, 0, inv.getSizeInventory, false))) {
        if(slotNum < inv.getSizeInventory + 9){
          if(!mergeItemStack(itemstack1, inv.getSizeInventory + 9, inv.getSizeInventory + 9 + 27, false)) return ItemStack.EMPTY
        } else {
          if(!mergeItemStack(itemstack1, inv.getSizeInventory, inv.getSizeInventory + 9, false)) return ItemStack.EMPTY
        }
      }
      if (itemstack1.getCount == 0) {
        slot.putStack(new ItemStack(Items.AIR, 0))
      } else {
        slot.onSlotChanged
      }
    }
    itemstack
  }
}