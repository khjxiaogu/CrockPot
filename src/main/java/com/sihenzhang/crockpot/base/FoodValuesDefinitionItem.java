package com.sihenzhang.crockpot.base;

import com.google.gson.*;
import com.sihenzhang.crockpot.util.JsonUtils;
import net.minecraft.item.Item;
import net.minecraft.util.JSONUtils;

import java.lang.reflect.Type;
import java.util.Objects;

public final class FoodValuesDefinitionItem {
    final Item item;
    final FoodValues foodValues;

    public FoodValuesDefinitionItem(Item item, FoodValues foodValues) {
        this.item = item;
        this.foodValues = foodValues;
    }

    public Item getItem() {
        return item;
    }

    public FoodValues getFoodValues() {
        return foodValues;
    }

    public static final class Serializer implements JsonDeserializer<FoodValuesDefinitionItem>, JsonSerializer<FoodValuesDefinitionItem> {
        @Override
        public FoodValuesDefinitionItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            Item item = JsonUtils.getAsItem(object, "item");
            FoodValues foodValues = FoodValues.fromJson(JSONUtils.getAsJsonObject(object, "values"));
            return new FoodValuesDefinitionItem(item, foodValues);
        }

        @Override
        public JsonElement serialize(FoodValuesDefinitionItem src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("item", Objects.requireNonNull(src.item.getRegistryName()).toString());
            object.add("values", src.foodValues.toJson());
            return object;
        }
    }
}
