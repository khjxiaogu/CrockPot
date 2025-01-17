package com.sihenzhang.crockpot.event;

import com.sihenzhang.crockpot.CrockPot;
import com.sihenzhang.crockpot.CrockPotRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CrockPot.MOD_ID)
public class WitherResistancePotionEffectEvent {
    @SubscribeEvent
    public static void onWitherPotionApply(PotionEvent.PotionApplicableEvent event) {
        // Avoid adding wither effect to entity
        if (event.getPotionEffect().getEffect() == Effects.WITHER && event.getEntityLiving().hasEffect(CrockPotRegistry.witherResistanceEffect)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onWitherResistancePotionAdded(PotionEvent.PotionAddedEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        // Remove exist wither effect
        if (event.getPotionEffect().getEffect() == CrockPotRegistry.witherResistanceEffect && livingEntity.hasEffect(Effects.WITHER)) {
            livingEntity.removeEffect(Effects.WITHER);
        }
    }
}
