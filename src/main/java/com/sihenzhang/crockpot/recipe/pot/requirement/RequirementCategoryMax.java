package com.sihenzhang.crockpot.recipe.pot.requirement;

import com.sihenzhang.crockpot.base.FoodCategory;
import com.sihenzhang.crockpot.recipe.pot.CrockPotRecipeInput;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.EnumUtils;

public class RequirementCategoryMax implements IRequirement {
    FoodCategory category;
    float max;

    public RequirementCategoryMax(FoodCategory category, float max) {
        this.category = category;
        this.max = max;
    }

    public RequirementCategoryMax(CompoundNBT nbt) {
        deserializeNBT(nbt);
    }

    @Override
    public boolean test(CrockPotRecipeInput recipeInput) {
        return recipeInput.mergedFoodValues.get(category) <= max;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(RequirementConstants.TYPE, RequirementType.CATEGORY_MAX.name().toLowerCase());
        nbt.putString(RequirementConstants.CATEGORY, category.name());
        nbt.putFloat(RequirementConstants.MAX, max);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (!RequirementType.CATEGORY_MAX.name().equals(nbt.getString(RequirementConstants.TYPE).toUpperCase())) {
            throw new IllegalArgumentException(RequirementConstants.REQUIREMENT_TYPE_NOT_MATCH);
        }
        this.category = EnumUtils.getEnum(FoodCategory.class, nbt.getString(RequirementConstants.CATEGORY).toUpperCase());
        this.max = nbt.getFloat(RequirementConstants.MAX);
    }
}
