package com.sihenzhang.crockpot.item;

import com.sihenzhang.crockpot.CrockPot;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CollectedDustItem extends Item {
    public CollectedDustItem() {
        super(new Properties().tab(CrockPot.ITEM_GROUP));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("tooltip.crockpot.collected_dust").withStyle(TextFormatting.GOLD));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
