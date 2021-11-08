package com.telepathicgrunt.the_bumblezone.advancements;

import com.google.gson.JsonObject;
import com.telepathicgrunt.the_bumblezone.Bumblezone;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class ProtectionOfTheHiveDefenseTrigger extends AbstractCriterionTrigger<ProtectionOfTheHiveDefenseTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(Bumblezone.MODID, "protection_of_the_hive_defense");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ProtectionOfTheHiveDefenseTrigger.Instance createInstance(JsonObject jsonObject, EntityPredicate.AndPredicate predicate, ConditionArrayParser conditionArrayParser) {
        EntityPredicate.AndPredicate entityPredicate = EntityPredicate.AndPredicate.fromJson(jsonObject, "entity", conditionArrayParser);
        return new ProtectionOfTheHiveDefenseTrigger.Instance(predicate, entityPredicate);
    }

    public void trigger(ServerPlayerEntity serverPlayerEntity, Entity entity) {
        LootContext lootcontext = EntityPredicate.createContext(serverPlayerEntity, entity);
        this.trigger(serverPlayerEntity, (instance) -> instance.matches(lootcontext));
    }

    public static class Instance extends CriterionInstance {
        private final EntityPredicate.AndPredicate attackerEntityPredicate;

        public Instance(EntityPredicate.AndPredicate predicate, EntityPredicate.AndPredicate attackerEntityPredicate) {
            super(ProtectionOfTheHiveDefenseTrigger.ID, predicate);
            this.attackerEntityPredicate = attackerEntityPredicate;
        }

        public boolean matches(LootContext lootContext) {
            return this.attackerEntityPredicate.matches(lootContext);
        }

        @Override
        public JsonObject serializeToJson(ConditionArraySerializer conditionArraySerializer) {
            JsonObject jsonobject = super.serializeToJson(conditionArraySerializer);
            jsonobject.add("entity", this.attackerEntityPredicate.toJson(conditionArraySerializer));
            return jsonobject;
        }
    }
}
