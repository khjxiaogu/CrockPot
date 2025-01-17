package com.sihenzhang.crockpot.recipe.pot;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sihenzhang.crockpot.recipe.pot.requirement.IRequirement;
import com.sihenzhang.crockpot.recipe.pot.requirement.RequirementUtil;
import com.sihenzhang.crockpot.util.NbtUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class CrockPotRecipe implements INBTSerializable<CompoundNBT>, Predicate<CrockPotRecipeInput> {
    List<IRequirement> requirements = new ArrayList<>();
    int priority, weight, cookTime, potLevel;
    ItemStack result;

    public static final CrockPotRecipe EMPTY = new CrockPotRecipe(0, 0, 0, 0, ItemStack.EMPTY);

    public CrockPotRecipe(int priority, int weight, int cookTime, int potLevel, ItemStack result) {
        this.priority = priority;
        this.weight = weight;
        this.result = result;
        this.cookTime = cookTime;
        this.potLevel = potLevel;
    }

    public boolean isEmpty() {
        return this.result.isEmpty();
    }

    public CrockPotRecipe(CompoundNBT nbt) {
        deserializeNBT(nbt);
    }

    public List<IRequirement> getRequirements() {
        return requirements;
    }

    public int getPriority() {
        return priority;
    }

    public int getWeight() {
        return weight;
    }

    public int getCookTime() {
        return cookTime;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getPotLevel() {
        return potLevel;
    }

    public void addRequirement(IRequirement requirement) {
        this.requirements.add(requirement);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT req = new ListNBT();
        requirements.stream().map(IRequirement::serializeNBT).forEach(req::add);
        nbt.put("requirements", req);
        nbt.putInt("priority", priority);
        nbt.putInt("weight", weight);
        nbt.put("result", result.serializeNBT());
        nbt.putInt("cookTime", cookTime);
        nbt.putInt("potLevel", potLevel);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.priority = nbt.getInt("priority");
        this.weight = nbt.getInt("weight");
        this.cookTime = nbt.getInt("cookTime");
        this.potLevel = nbt.getInt("potLevel");
        this.result = ItemStack.of((CompoundNBT) Objects.requireNonNull(nbt.get("result")));
        ListNBT requirements = (ListNBT) nbt.get("requirements");
        assert requirements != null;
        requirements.stream().map(RequirementUtil::deserialize).forEach(this.requirements::add);
    }

    @Override
    public boolean test(CrockPotRecipeInput recipeInput) {
        return recipeInput.potLevel >= this.potLevel && requirements.stream().allMatch(r -> r.test(recipeInput));
    }

    public static class Serializer implements JsonDeserializer<CrockPotRecipe>, JsonSerializer<CrockPotRecipe> {
        @Override
        public CrockPotRecipe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return new CrockPotRecipe(JsonToNBT.parseTag(json.toString()));
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(e);
            }
        }

        @Override
        public JsonElement serialize(CrockPotRecipe src, Type typeOfSrc, JsonSerializationContext context) {
            return NbtUtils.convertToJson(src.serializeNBT());
        }
    }
}
