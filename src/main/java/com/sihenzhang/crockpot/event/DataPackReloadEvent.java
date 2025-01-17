package com.sihenzhang.crockpot.event;

import com.sihenzhang.crockpot.CrockPot;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CrockPot.MOD_ID)
public class DataPackReloadEvent {
    @SubscribeEvent
    public static void onReloading(AddReloadListenerEvent event) {
        event.addListener(CrockPot.CROCK_POT_RECIPE_MANAGER);
        event.addListener(CrockPot.FOOD_VALUES_MANAGER);
        event.addListener(CrockPot.PIGLIN_BARTERING_RECIPE_MANAGER);
        event.addListener(CrockPot.EXPLOSION_CRAFTING_RECIPE_MANAGER);
    }
}
