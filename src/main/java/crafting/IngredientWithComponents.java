package crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientWithComponents(Ingredient ingredient, DataComponentPatch components) {

    public static final Codec<IngredientWithComponents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("item").forGetter(IngredientWithComponents::ingredient),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(IngredientWithComponents::components)
            ).apply(instance, IngredientWithComponents::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithComponents> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, IngredientWithComponents::ingredient,
                    DataComponentPatch.STREAM_CODEC, IngredientWithComponents::components,
                    IngredientWithComponents::new);
}