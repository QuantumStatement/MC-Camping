package com.rikmuld.camping.inventory.objs

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.client.gui.inventory.GuiContainer
import com.rikmuld.corerm.client.GuiContainerSimple
import com.rikmuld.camping.Lib.TextureInfo
import com.rikmuld.camping.objs.tile.TileCampfire
import net.minecraft.util.StatCollector
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
import net.minecraft.inventory.ICrafting
import com.rikmuld.camping.objs.Objs
import java.util.ArrayList
import com.rikmuld.camping.objs.tile.TileCampfire
import com.rikmuld.camping.objs.tile.TileCampfireCook
import com.rikmuld.camping.inventory.SlotCooking

class GuiCampfire(player: EntityPlayer, tile: IInventory) extends GuiContainerSimple(new ContainerCampfire(player, tile)) {
  ySize = 120

  var fire = tile.asInstanceOf[TileCampfire]  
  
  def getTexture: String = TextureInfo.GUI_CAMPFIRE
  def getName: String = ""
  def hasName: Boolean = false
  override def drawGuiContainerForegroundLayer(par1: Int, par2: Int) {
    val time = fire.time
    val timeLeft = (if (fire.color == 16) "" else ("�" + TextInfo.COLOURS_DYE(fire.color))) + (time / 1200).toString + ":" + (if (((time % 1200) / 20).toString.length == 1) ("0" + ((time % 1200) / 20).toString) else (((time % 1200) / 20).toString))
    fontRendererObj.drawString(StatCollector.translateToLocal(timeLeft), 92, 16, 4210752)
  }
}

class ContainerCampfire(player: EntityPlayer, tile: IInventory) extends RMContainerTile(player, tile) {
  addSlotToContainer(new SlotItemsOnly(tile, 0, 71, 12, Items.dye))
  this.addSlots(player.inventory, 0, 1, 9, 8, 96)
  this.addSlots(player.inventory, 9, 3, 9, 8, 38)

  override def transferStackInSlot(player: EntityPlayer, slotNum: Int): ItemStack = {
    var itemstack: ItemStack = null
    val slot = inventorySlots.get(slotNum).asInstanceOf[Slot]
    if ((slot != null) && slot.getHasStack) {
      val itemstack1 = slot.getStack
      itemstack = itemstack1.copy()
      if (slotNum < tile.getSizeInventory) {
        if (!mergeItemStack(itemstack1, tile.getSizeInventory, inventorySlots.size, true)) return null
      } else {
        if (itemstack.getItem == Items.dye) {
          if (!mergeItemStack(itemstack1, 0, 1, false)) return null
        } else return null
      }
      if (itemstack1.stackSize == 0) {
        slot.putStack(null)
      } else {
        slot.onSlotChanged()
      }
    }
    itemstack
  }
}

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
  def getGuiLeft(): Int = guiLeft
  def getGuiTop(): Int = guiTop
}

class ContainerCampfireCook(player: EntityPlayer, tile: IInventory) extends RMContainerTile(player, tile) {
  val fire = tile.asInstanceOf[TileCampfireCook]
  val slots = new ArrayList[SlotCooking]()

  addSlotToContainer(new SlotItemsOnly(tile, 0, 80, 84, Items.coal))
  addSlotToContainer(new SlotItemsOnly(tile, 1, 150, 9, new ItemStack(Objs.kit, 1, 1), new ItemStack(Objs.kit, 1, 3), new ItemStack(Objs.kit, 1, 2)))
  for (i <- 0 until 10) {
    val slot = new SlotCooking(tile, i + 2, 0, 0)
    slots.add(slot)
    addSlotToContainer(slot)
  }
  fire.setSlots(slots)
  this.addSlots(player.inventory, 0, 1, 9, 8, 164)
  this.addSlots(player.inventory, 9, 3, 9, 8, 106)

  override def addCraftingToCrafters(crafting: ICrafting) {
    super.addCraftingToCrafters(crafting)
    for (i <- 0 until fire.cookProgress.length) {
      crafting.sendProgressBarUpdate(this, i, fire.cookProgress(i))
    }
  }
  override def transferStackInSlot(player: EntityPlayer, slotNum: Int): ItemStack = {
    var itemstack: ItemStack = null
    val slot = inventorySlots.get(slotNum).asInstanceOf[Slot]
    if ((slot != null) && slot.getHasStack) {
      val itemstack1 = slot.getStack
      itemstack = itemstack1.copy()
      if (slotNum < fire.getSizeInventory) {
        if (!mergeItemStack(itemstack1, fire.getSizeInventory, inventorySlots.size, true)) return null
      } else {
        if (itemstack.getItem == Items.coal) {
          if (!mergeItemStack(itemstack1, 0, 1, false)) return null
        } else if (itemstack.getItem == Objs.kit) {
          if (!mergeItemStack(itemstack1, 1, 2, false)) return null
        } else return null
      }
      if (itemstack1.stackSize == 0) {
        slot.putStack(null)
      } else {
        slot.onSlotChanged()
      }
    }
    itemstack
  }
}