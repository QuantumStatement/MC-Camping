package com.rikmuld.camping.advancements

import com.google.gson.{JsonDeserializationContext, JsonObject}
import com.rikmuld.camping.Lib.AdvancementInfo._
import com.rikmuld.camping.objs.entity.Camper
import com.rikmuld.corerm.advancements.{AdvancementTrigger, TriggerInstance}
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.ResourceLocation

object CamperInteract {
  class Trigger extends AdvancementTrigger[Camper, Instance] {
    protected val id: ResourceLocation =
      CAMPER_INTERACT

    override def deserializeInstance(json: JsonObject, context: JsonDeserializationContext): Instance =
      new Instance(Option(json.get("camper")) map EntityPredicate.deserialize)
  }

  protected class Instance(item: Option[EntityPredicate]) extends TriggerInstance[Camper](CAMPER_INTERACT) {
    def test(player: EntityPlayerMP, camper: Camper): Boolean =
      item.fold(true)(_.test(player, camper))
  }
}