package crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;

import java.util.HashMap;

public class ComponentMatchingRecipeSerializer {
    private static final StreamCodec<ByteBuf, Character> CHAR_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(s -> s.charAt(0), String::valueOf);

    private static final Codec<Character> SINGLE_CHAR_CODEC = Codec.STRING.comapFlatMap(
            s -> s.length() == 1 ? DataResult.success(s.charAt(0))
                    : DataResult.error(() -> "Key must be a single character"),
            String::valueOf);

    public static final MapCodec<ComponentMatchingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(r -> r.category),
            Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
            Codec.STRING.listOf().fieldOf("pattern").forGetter(r -> r.pattern),
            Codec.unboundedMap(SINGLE_CHAR_CODEC, IngredientWithComponents.CODEC).fieldOf("key").forGetter(r -> r.key),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, ComponentMatchingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComponentMatchingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    CraftingBookCategory.STREAM_CODEC, r -> r.category,
                    ByteBufCodecs.STRING_UTF8, r -> r.group,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), r -> r.pattern,
                    ByteBufCodecs.map(HashMap::new, CHAR_STREAM_CODEC, IngredientWithComponents.STREAM_CODEC), r -> r.key,
                    ItemStackTemplate.STREAM_CODEC, r -> r.result,
                    ComponentMatchingRecipe::new);
}