package com.nomiceu.nomilabs.groovy;

import com.brandon3055.draconicevolution.api.fusioncrafting.IFusionRecipe;
import com.brandon3055.draconicevolution.lib.RecipeManager;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.compat.mods.jei.JeiPlugin;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import com.nomiceu.nomilabs.LabsValues;
import com.nomiceu.nomilabs.integration.jei.JEIPlugin;
import com.nomiceu.nomilabs.util.LabsTranslate;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.TooltipHelper;
import groovy.lang.Closure;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The interface for groovy to interact with.
 */
@SuppressWarnings("unused")
public class GroovyHelpers {
    public static class TranslationHelpers {
        public static String translate(String key, Object... params) {
            return LabsTranslate.translate(key, params);
        }

        public static String translateFormat(String key, TooltipHelper.GTFormatCode format, Object... params) {
            return LabsTranslate.translateFormat(key, format, params);
        }

        public static String format(String str, TextFormatting... formats) {
            return LabsTranslate.format(str, formats);
        }

        public static String format(String str, TooltipHelper.GTFormatCode... formats) {
            return LabsTranslate.format(str, formats);
        }

        public static String format(String str, LabsTranslate.Format... formats) {
            return LabsTranslate.format(str, formats);
        }
    }
    public static class SafeMethodHelpers {
        /**
         * Calls a declared instance method of a caller safely. Searches for the method in that class and its subclasses.
         */
        @Nullable
        public static Object callInstanceMethod(Object caller, @NotNull String methodName, @Nullable List<Object> params) {
            return callMethod(caller.getClass(), caller, methodName, params, false);
        }

        /**
         * Calls a declared instance method of a specific class safely. Only searches for that method in that class.
         */
        @Nullable
        public static Object callInstanceMethodOfClass(Class<?> clazz, Object caller, @NotNull String methodName, @Nullable List<Object> params) {
            return callMethod(clazz, caller, methodName, params, true);
        }

        /**
         * Calls a declared static method of a class safely. Only searches for that method in that class.
         */
        @Nullable
        public static Object callStaticMethod(Class<?> clazz, @NotNull String methodName, @Nullable List<Object> params) {
            return callMethod(clazz, null, methodName, params, true);
        }

        /**
         * Call a method of a class safely.
         * @param clazz Class to call.
         * @param caller Caller. Null if static.
         * @param methodName Name of method
         * @param params Params. Null or Empty List if none
         * @param strict Whether to be strict. Strict means that checking for the method is only done in the class provided.
         */
        @Nullable
        public static Object callMethod(Class<?> clazz, @Nullable Object caller, @NotNull String methodName, @Nullable List<Object> params, boolean strict) {
            try {
                var paramArray = params != null ?
                        params.toArray() :
                        new Object[0];
                var paramClasses = params != null ?
                        params.stream().map(Object::getClass).toArray(Class<?>[]::new) :
                        new Class<?>[0];
                Method method = strict ? clazz.getDeclaredMethod(methodName, paramClasses) : clazz.getMethod(methodName, paramClasses);
                return method.invoke(caller, paramArray);
            } catch (NoSuchMethodException | ClassCastException | NoClassDefFoundError | InvocationTargetException |
                     IllegalAccessException | IllegalArgumentException e) {
                // Doesn't throw it, but lets us know about the issue.
                GroovyLog.get().exception(e);
                return null;
            }
        }
    }
    public static class JEIHelpers {
        public static void addDescription(ItemStack stack, String... description) {
            JEIPlugin.addGroovyDescription(stack, description);
        }
        public static void addRecipeOutputTooltip(ItemStack stack, String... tooltip) {
            JEIPlugin.addGroovyRecipeOutputTooltip(stack, tooltip);
        }
        public static void addRecipeOutputTooltip(ItemStack stack, ResourceLocation recipeName, String... tooltip) {
            JEIPlugin.addGroovyRecipeOutputTooltip(stack, recipeName, tooltip);
        }
    }
    public static class MaterialHelpers {
        public static void hideMaterial(Material material) {
            MaterialHelper.forMaterialItem(material, JeiPlugin::hideItem);
            MaterialHelper.forMaterialFluid(material, (fluid) -> JeiPlugin.HIDDEN_FLUIDS.add(toFluidStack(fluid)));
        }
        public static void removeAndHideMaterial(Material material) {
            MaterialHelper.forMaterialItem(material, (stack) ->
                    ModSupport.JEI.get().removeAndHide(IngredientHelper.toIIngredient(stack)));
            // Normal Hiding for Fluids, they don't have recipes
            MaterialHelper.forMaterialFluid(material, (fluid) -> JeiPlugin.HIDDEN_FLUIDS.add(toFluidStack(fluid)));
        }
        public static void yeetMaterial(Material material) {
            removeAndHideMaterial(material);
        }
        public static void forMaterial(Material material, Closure<ItemStack> itemAction, Closure<Fluid> fluidAction) {
            forMaterialItem(material, itemAction);
            forMaterialFluid(material, fluidAction);
        }
        public static void forMaterialItem(Material material, Closure<ItemStack> action) {
            MaterialHelper.forMaterialItem(material, (stack) -> ClosureHelper.call(action, stack));
        }
        public static void forMaterialFluid(Material material, Closure<Fluid> action) {
            MaterialHelper.forMaterialFluid(material, (fluid) -> ClosureHelper.call(action, fluid));
        }

        private static FluidStack toFluidStack(Fluid fluid) {
            return new FluidStack(fluid, 1);
        }
    }
    public static class RecipeRecyclingHelpers {
        public static void replaceRecipeShaped(String name, ItemStack output, List<List<IIngredient>> inputs) {
            if (name.contains(":"))
                replaceRecipeShaped(new ResourceLocation(name), output, inputs);
            else
                replaceRecipeShaped(GTUtility.gregtechId(name), output, inputs);
        }

        public static void replaceRecipeShaped(ResourceLocation name, ItemStack output, List<List<IIngredient>> inputs) {
            ReplaceRecipe.replaceRecipeShaped(name, output, inputs);
        }

        public static void replaceRecipeShaped(ItemStack oldOutput, ItemStack newOutput, List<List<IIngredient>> inputs) {
            ReplaceRecipe.replaceRecipeShaped(oldOutput, newOutput,inputs);
        }

        public static void replaceRecipeOutput(String name, ItemStack output) {
            if (name.contains(":"))
                replaceRecipeOutput(new ResourceLocation(name), output);
            else
                replaceRecipeOutput(GTUtility.gregtechId(name), output);
        }

        public static void replaceRecipeOutput(ResourceLocation name, ItemStack newOutput) {
            ReplaceRecipe.replaceRecipeOutput(name, newOutput);
        }

        public static void replaceRecipeOutput(ItemStack oldOutput, ItemStack newOutput) {
            ReplaceRecipe.replaceRecipeOutput(oldOutput, newOutput);
        }

        public static void replaceRecipeInput(String name, List<List<IIngredient>> inputs) {
            if (name.contains(":"))
                replaceRecipeInput(new ResourceLocation(name), inputs);
            else
                replaceRecipeInput(GTUtility.gregtechId(name), inputs);
        }

        public static void replaceRecipeInput(ResourceLocation name, List<List<IIngredient>> newInputs) {
            ReplaceRecipe.replaceRecipeInput(name, newInputs);
        }

        public static void replaceRecipeInput(ItemStack oldOutput, List<List<IIngredient>> newInputs) {
            ReplaceRecipe.replaceRecipeInput(oldOutput, newInputs);
        }

        public static void createRecipe(String name, ItemStack output, List<List<IIngredient>> input) {
            ReplaceRecipe.createRecipe(name, output, input);
        }

        public static void createRecipe(ItemStack output, List<List<IIngredient>> input) {
            ReplaceRecipe.createRecipe(output, input);
        }

        public static void changeStackRecycling(ItemStack output, List<IIngredient> ingredients) {
            ReplaceRecipe.changeStackRecycling(output, ingredients);
        }
        public static void removeStackRecycling(ItemStack output) {
            ReplaceRecipe.changeStackRecycling(output, Collections.emptyList());
        }
    }
    public static class ChangeCompositionHelpers {
        public static CompositionBuilder changeComposition(Material material) {
            return new CompositionBuilder(material);
        }

        public static CompositionBuilder replaceDeomposition(MaterialStack material) {
            return new CompositionBuilder(material.material);
        }

        public static CompositionBuilder changeComposition(ItemStack stack) {
            MaterialStack mat = CompositionBuilder.getMatFromStack(stack);
            if (mat == null) return new CompositionBuilder(
                    GregTechAPI.materialManager.getRegistry(GTValues.MODID).getFallbackMaterial());

            return new CompositionBuilder(mat.material);
        }

        public static CompositionBuilder changeComposition(FluidStack fluid) {
            MaterialStack mat = CompositionBuilder.getMatFromFluid(fluid);
            if (mat == null) return new CompositionBuilder(
                    GregTechAPI.materialManager.getRegistry(GTValues.MODID).getFallbackMaterial());

            return new CompositionBuilder(mat.material);
        }
    }
    public static class GTRecipeHelpers {
        public static ChancedItemOutput chanced(ItemStack stack, int chance, int chanceBoost) {
            return new ChancedItemOutput(stack, chance, chanceBoost);
        }

        public static ChancedFluidOutput chanced(FluidStack fluid, int chance, int chanceBoost) {
            return new ChancedFluidOutput(fluid, chance, chanceBoost);
        }
    }

    public static class MiscHelpers {
        public static void removeDraconicFusionRecipe(ItemStack catalyst, ItemStack result) {
            if (!Loader.isModLoaded(LabsValues.DRACONIC_MODID)) return;

            //noinspection SimplifyStreamApiCallChains
            for (IFusionRecipe recipe : RecipeManager.FUSION_REGISTRY.getRecipes().stream()
                    .filter(x -> x.getRecipeCatalyst().isItemEqual(catalyst) && x.getRecipeOutput(catalyst).isItemEqual(result))
                    .collect(Collectors.toList())) {
                ModSupport.DRACONIC_EVOLUTION.get().fusion.remove(recipe);
            }
        }
    }
}
