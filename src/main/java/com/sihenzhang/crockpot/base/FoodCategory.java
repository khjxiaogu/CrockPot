package com.sihenzhang.crockpot.base;

import com.sihenzhang.crockpot.CrockPotRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Color;

public enum FoodCategory {
    MEAT("#FFABC7"),
    MONSTER("#D700FF"),
    FISH("#006BFF"),
    EGG("#00FFBB"),
    FRUIT("#FF6B00"),
    VEGGIE("#00FF00"),
    DAIRY("#00C7FF"),
    SWEETENER("#FFFF00"),
    FROZEN("#82FFFF"),
    INEDIBLE("9B9B9B");

    public final Color color;

    FoodCategory(String colorHex) {
        this.color = Color.parseColor(colorHex);
    }

    public static ItemStack getItemStack(FoodCategory category) {
        return CrockPotRegistry.foodCategoryItems.get(category).getDefaultInstance();
    }
}
