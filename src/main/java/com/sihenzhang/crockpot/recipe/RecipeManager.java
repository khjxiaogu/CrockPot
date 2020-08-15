package com.sihenzhang.crockpot.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ParametersAreNonnullByDefault
public class RecipeManager extends JsonReloadListener {
    private static final Gson GSON_INSTANCE = new GsonBuilder().registerTypeAdapter(Recipe.class, new Recipe.Serializer()).create();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private List<Recipe> recipes = ImmutableList.of();

    private static int workers = 0;
    private static final Executor executor = Executors.newFixedThreadPool(4, (r) -> new Thread(r, "CrockpotMatchingWorker-" + ++workers));

    public RecipeManager() {
        super(GSON_INSTANCE, "crock_pot");
    }

    public FutureRecipe match(RecipeInput input) {
        FutureRecipe result = new FutureRecipe();
        executor.execute(() -> result.setResult(matchBlocking(input)));
        return result;
    }

    private Recipe matchBlocking(RecipeInput input) {
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
        int rand = RANDOM.nextInt(sum) + 1;
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
        output.sort(Comparator.comparingInt(r -> ((Recipe) r).priority).reversed());
        recipes = ImmutableList.copyOf(output);
        profilerIn.endStartSection("crockPotRecipesLoad");
        LOGGER.info("Loaded {} crock pot recipes", recipes.size());
    }
}
