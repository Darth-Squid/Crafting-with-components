package crafting;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.minecraft.world.item.crafting.RecipeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftingWithComponents implements ModInitializer {
	public static final String MOD_ID = "crafting-with-components";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static RecipeSerializer<ComponentMatchingRecipe> COMPONENT_MATCHING_SERIALIZER;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		COMPONENT_MATCHING_SERIALIZER = Registry.register(
				BuiltInRegistries.RECIPE_SERIALIZER,
				Identifier.fromNamespaceAndPath("cwc", "component_matching"),
				new RecipeSerializer<>(
						ComponentMatchingRecipeSerializer.CODEC,
						ComponentMatchingRecipeSerializer.STREAM_CODEC));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
