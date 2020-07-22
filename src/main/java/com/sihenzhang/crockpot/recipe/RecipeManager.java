package com.sihenzhang.crockpot.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sihenzhang.crockpot.base.CrockPotIngredientType;
import com.sihenzhang.crockpot.recipe.requirements.RequirementIngredientMax;
import com.sihenzhang.crockpot.recipe.requirements.RequirementIngredientMinExclusive;
import com.sihenzhang.crockpot.recipe.requirements.RequirementMustContainItem;
import com.sihenzhang.crockpot.recipe.requirements.RequirementType;
import com.sihenzhang.crockpot.registry.CrockPotRegistry;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class RecipeManager extends JsonReloadListener {
    private static final Gson GSON_INSTANCE = new GsonBuilder().registerTypeAdapter(Recipe.class, new Recipe.Serializer()).create();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private List<Recipe> recipes = ImmutableList.of();

    public RecipeManager() {
        super(GSON_INSTANCE, "crock_pot");
    }

    public Recipe match(RecipeInput input) {
        Iterator<Recipe> itr = recipes.iterator();
        Recipe r;

        List<Recipe> matched = new LinkedList<>();

        boolean isFirst = true;
        int p = 0;
        while (itr.hasNext()) {
            r = itr.next();
            if (isFirst) {
                if (r.test(input)) {
                    p = r.priority;
                    matched.add(r);
                    isFirst = false;
                }
            } else {
                if (r.priority != p) {
                    break;
                } else {
                    if (r.test(input)) {
                        matched.add(r);
                    }
                }
            }
        }
        if (matched.isEmpty()) return null;
        int sum = 0;
        for (Recipe e : matched) {
            sum += e.weight;
        }
        int rand = RANDOM.nextInt(sum);
        for (Recipe e : matched) {
            rand -= e.weight;
            if (rand <= 0) {
                return e;
            }
        }
        return null;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        profilerIn.startSection("crockPotRecipesLoad");
        List<Recipe> output = new LinkedList<>();
        for (Map.Entry<ResourceLocation, JsonObject> entry : objectIn.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            if (resourceLocation.getPath().startsWith("_"))
                continue;
            try {
                Recipe recipe = GSON_INSTANCE.fromJson(entry.getValue(), Recipe.class);
                if (recipe != null && !recipe.getResult().isEmpty()) {
                    output.removeIf(recipe1 -> recipe.getResult() == recipe1.getResult());
                    output.add(recipe);
                }
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error("Parsing error loading crock pot recipe {}", resourceLocation, exception);
            }
        }
//        Recipe testRecipe = new Recipe(10, 1, 600, 0, new ItemStack(CrockPotRegistry.fishSticks.get()));
//        testRecipe.addRequirement(new RequirementIngredientMinExclusive(CrockPotIngredientType.FISH, 0.0F), RequirementType.REQUIRED);
//        List<Item> requireItems = new LinkedList<>();
//        requireItems.add(Items.STICK);
//        requireItems.add(Items.BAMBOO);
//        testRecipe.addRequirement(new RequirementMustContainItem(requireItems, 1), RequirementType.REQUIRED);
//        testRecipe.addRequirement(new RequirementIngredientMax(CrockPotIngredientType.INEDIBLE, 1.0F), RequirementType.REQUIRED);
//        output.add(testRecipe);
        output.sort(Comparator.comparingInt(r -> ((Recipe) r).priority).reversed());
        recipes = ImmutableList.copyOf(output);
        profilerIn.endStartSection("crockPotRecipesLoad");
        LOGGER.info("Loaded {} crock pot recipes", recipes.size());
    }
}