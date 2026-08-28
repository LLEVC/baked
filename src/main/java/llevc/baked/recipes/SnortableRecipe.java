package llevc.baked.recipes;

import com.mojang.serialization.MapCodec;
import llevc.baked.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

import java.util.Objects;

public class SnortableRecipe extends CustomRecipe {
    public SnortableRecipe() {
        super();
    }

    public boolean matches(CraftingInput recipeInput, Level level) {
        boolean potion = false;
        boolean flask = false;

        for (int i = 0; i < recipeInput.size(); i++) {
            ItemStack itemStack = recipeInput.getItem(i);
            Block block = Objects.requireNonNullElse(Block.byItem(itemStack.getItem()), Blocks.AIR);
            if (block != Blocks.AIR || itemStack.is(Items.BRICK)) {
                if (block != Blocks.AIR && !potion) {
                    potion = true;
                } else if (block != Blocks.AIR && potion) {
                    return false;
                }
                if (itemStack.is(Items.BRICK) && !flask) {
                    flask = true;
                } else if (itemStack.is(Items.BRICK) && flask) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return (potion && flask && recipeInput.ingredientCount() <= 2);
    }

    public ItemStack assemble(CraftingInput recipeInput) {
        ItemStack result = ItemStack.EMPTY;
        Block block = Blocks.AIR;
        boolean flask = false;
        boolean overflow = false;

        for (int i = 0; i < recipeInput.size(); i++) {
            ItemStack itemStack = recipeInput.getItem(i);
            Block block1 = Objects.requireNonNullElse(Block.byItem(itemStack.getItem()), Blocks.AIR);
            if (block1 != Blocks.AIR) {
                if (block == Blocks.AIR) {
                    block = block1;
                } else {
                    overflow = true;
                }
            } else if (itemStack.is(Items.BRICK)) {

                if (itemStack.is(Items.BRICK) && !flask) {
                    flask = true;
                } else if (itemStack.is(Items.BRICK) && flask) {
                    overflow = true;
                }
            } else if (!itemStack.isEmpty()) {
                overflow = true;
            }
        }

        if (!overflow && flask && block != Blocks.AIR) {
            ItemStack yes = ModItems.createSnortable(block);
            result = yes;
        }

        return result;
    }

    public static final SnortableRecipe instance = new SnortableRecipe();
    public static final MapCodec<SnortableRecipe> MAP_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, SnortableRecipe> STREAM_CODEC;
    public static final RecipeSerializer<SnortableRecipe> SERIALIZER;

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    static {
        MAP_CODEC = MapCodec.unit(instance);
        STREAM_CODEC = StreamCodec.unit(instance);
        SERIALIZER = new RecipeSerializer(MAP_CODEC, STREAM_CODEC);
    }
}
