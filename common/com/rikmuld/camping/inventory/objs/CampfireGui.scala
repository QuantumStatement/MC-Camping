package com.rikmuld.camping.inventory.objs

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.client.gui.inventory.GuiContainer
import com.rikmuld.corerm.client.GuiContainerSimple
import com.rikmuld.camping.Lib.TextureInfo
import com.rikmuld.camping.objs.tile.TileCampfire
import org.lwjgl.opengl.GL11
import net.minecraft.util.ResourceLocation
import com.rikmuld.corerm.Lib.TextInfo
import com.rikmuld.camping.objs.tile.TileCampfireCook
import com.rikmuld.corerm.inventory.RMContainerTile
import com.rikmuld.corerm.CoreUtils._
import net.minecraft.item.ItemStack
import com.rikmuld.corerm.inventory.SlotItemsOnly
import net.minecraft.init.Items
import net.minecraft.inventory.Slot
import com.rikmuld.camping.objs.Objs
import java.util.ArrayList
import com.rikmuld.camping.objs.tile.TileCampfire
import com.rikmuld.camping.objs.tile.TileCampfireCook
import com.rikmuld.camping.inventory.SlotCooking
import net.minecraft.inventory.IContainerListener
import com.rikmuld.camping.objs.ItemDefinitions

class GuiCampfireCook(player: EntityPlayer, tile: IInventory) extends GuiContainer(new ContainerCampfireCook(player, tile)) {
  ySize = 188

  var fire = tile.asInstanceOf[TileCampfireCook]

  protected override def drawGuiContainerBackgroundLayer(mouseX: Float, mouseY: Int, partTicks: Int) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    mc.renderEngine.bindTexture(new ResourceLocation(TextureInfo.GUI_CAMPFIRE_COOK))
    var scale = fire.getScaledCoal(40).toInt
    scale += 1
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
    if (fire.equipment != null) {
      fire.equipment.drawGuiTexture(this)
    }
    drawTexturedModalRect(guiLeft + 66, (guiTop + 94) - scale, 176, 40 - scale, 44, scale)
    drawTexturedModalRect(guiLeft + 79, guiTop + 83, 79, 105, 18, 18)
    if (fire.equipment != null) {
      for (i <- 0 until fire.equipment.maxFood) {
        val scale2 = fire.getScaledcookProgress(10, i).toInt
        val isNotCooked = if (fire.getStackInSlot(i + 2) != null) fire.equipment.canCook(fire.getStackInSlot(i + 2)) else false
        drawTexturedModalRect(guiLeft + fire.equipment.slots(0)(i) + 16, guiTop + fire.equipment.slots(1)(i) + 2,
          223, 0, 3, 12)
        drawTexturedModalRect(guiLeft + fire.equipment.slots(0)(i) + 17, (guiTop + fire.equipment.slots(1)(i) + 13) - scale2,
          if (isNotCooked) 226 else 227, 11 - scale2, 1, scale2)
      }
    }
  }
}

class ContainerCampfireCook(player: EntityPlayer, tile: IInventory) extends RMContainerTile(player, tile) {
  val fire = tile.asInstanceOf[TileCampfireCook]
  val slots = new ArrayList[SlotCooking]()

  addSlotToContainer(new SlotItemsOnly(tile, 0, 80, 84, Items.COAL))
  addSlotToContainer(new SlotItemsOnly(tile, 1, 150, 9, new ItemStack(Objs.kit, 1, 1), new ItemStack(Objs.kit, 1, 3), new ItemStack(Objs.kit, 1, 2)))
  for (i <- 0 until 10) {
    val slot = new SlotCooking(tile, i + 2, 0, 0)
    slots.add(slot)
    addSlotToContainer(slot)
  }
  fire.setSlots(slots)
  this.addSlots(player.inventory, 0, 1, 9, 8, 164)
  this.addSlots(player.inventory, 9, 3, 9, 8, 106)

  override def addListener(crafting: IContainerListener) {
    super.addListener(crafting)
    for (i <- 0 until fire.cookProgress.length) {
      crafting.sendProgressBarUpdate(this, i, fire.cookProgress(i))
    }
  }
  override def transferStackInSlot(player: EntityPlayer, slotNum: Int): ItemStack = {
    var itemstack: ItemStack = ItemStack.EMPTY
    val slot = inventorySlots.get(slotNum).asInstanceOf[Slot]
    if ((slot != null) && slot.getHasStack) {
      val itemstack1 = slot.getStack
      itemstack = itemstack1.copy()
      if (slotNum < fire.getSizeInventory) {
        if (!mergeItemStack(itemstack1, fire.getSizeInventory, inventorySlots.size, false)) return ItemStack.EMPTY
      } else {
        var merged = false
        if (itemstack.getItem == Items.COAL) {
          if (mergeItemStack(itemstack1, 0, 1, false)) merged = true
        } else if (itemstack.getItem == Objs.kit && itemstack.getItemDamage != ItemDefinitions.Kit.USELESS && itemstack.getItemDamage != ItemDefinitions.Kit.EMPTY) {
          if (mergeItemStack(itemstack1, 1, 2, false)) merged = true
        } else if(!merged){
            if(slotNum < fire.getSizeInventory + 9){
              if(!mergeItemStack(itemstack1, fire.getSizeInventory + 9, fire.getSizeInventory + 9 + 27, false)) return ItemStack.EMPTY
            } else {
              if(!mergeItemStack(itemstack1, fire.getSizeInventory, fire.getSizeInventory + 9, false)) return ItemStack.EMPTY
            }
        }
      }
      if (itemstack1.getCount == 0) {
        slot.putStack(new ItemStack(Items.AIR, 0))
      } else {
        slot.onSlotChanged()
      }
    }
    itemstack
  }
}