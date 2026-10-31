package crafting;

import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static crafting.CraftingWithComponents.COMPONENT_MATCHING_SERIALIZER;

public class ComponentMatchingRecipe implements CraftingRecipe {

    public record Layout(int width, int height, List<Optional<IngredientWithComponents>> cells) {
        public static DataResult<Layout> create(List<String> pattern,
                                                Map<Character, IngredientWithComponents> key) {
            if (pattern.isEmpty()) {
                return DataResult.error(() -> "Pattern must have at least one row");
            }
            int width = pattern.get(0).length();
            int height = pattern.size();
            if (width == 0 || width > 3 || height > 3) {
                return DataResult.error(() -> "Pattern must be 1x1 to 3x3");
            }
            List<Optional<IngredientWithComponents>> cells = new ArrayList<>();
            for (String row : pattern) {
                if (row.length() != width) {
                    return DataResult.error(() -> "All pattern rows must be the same length");
                }
                for (char c : row.toCharArray()) {
                    if (c == ' ') {
                        cells.add(Optional.empty());
                    } else if (key.containsKey(c)) {
                        cells.add(Optional.of(key.get(c)));
                    } else {
                        return DataResult.error(() -> "Pattern uses undefined key '" + c + "'");
                    }
                }
            }
            return DataResult.success(new Layout(width, height, cells));
        }
    }

    final CraftingBookCategory category;
    final String group;
    final List<String> pattern;
    final Map<Character, IngredientWithComponents> key;
    final ItemStackTemplate result;
    private final Layout layout;
    private final PlacementInfo placementInfo;

    public ComponentMatchingRecipe(CraftingBookCategory category, String group,
                                   List<String> pattern, Map<Character, IngredientWithComponents> key,
                                   ItemStackTemplate result) {
        this.category = category;
        this.group = group;
        this.pattern = pattern;
        this.key = key;
        this.result = result;
        this.layout = Layout.create(pattern, key).getOrThrow(IllegalArgumentException::new);
        this.placementInfo = PlacementInfo.createFromOptionals(
                layout.cells().stream().map(c -> c.map(IngredientWithComponents::ingredient)).toList());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != layout.width() || input.height() != layout.height()) return false;
        return matchesAt(input, false) || matchesAt(input, true);
    }

    private boolean matchesAt(CraftingInput input, boolean mirrored) {
        for (int y = 0; y < layout.height(); y++) {
            for (int x = 0; x < layout.width(); x++) {
                int px = mirrored ? layout.width() - 1 - x : x;
                Optional<IngredientWithComponents> entry = layout.cells().get(px + y * layout.width());
                ItemStack stack = input.getItem(x, y);

                if (entry.isEmpty()) {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                } else {
                    if (!entry.get().ingredient().test(stack)) {
                        return false;
                    }
                    if (!matchesComponents(stack, entry.get().components())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean matchesComponents(ItemStack stack, DataComponentPatch required) {
        for (TypedDataComponent<?> component : required.split().added()) {
            Object actual = stack.get(component.type());

            if (component.value() instanceof CustomData expected) {
                if (!(actual instanceof CustomData have) || !have.matchedBy(expected.copyTag())) {
                    return false;
                }
            } else if (!component.value().equals(actual)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NonNull List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(
                layout.width(),
                layout.height(),
                layout.cells().stream()
                        .map(cell -> cell
                                .map(c -> c.ingredient().display())
                                .orElse(SlotDisplay.Empty.INSTANCE))
                        .toList(),
                new SlotDisplay.ItemStackSlotDisplay(result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return result.create();
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
        return COMPONENT_MATCHING_SERIALIZER;
    }

    @Override
    public PlacementInfo placementInfo() {
        return placementInfo;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }
}