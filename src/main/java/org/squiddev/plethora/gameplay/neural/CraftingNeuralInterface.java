package org.squiddev.plethora.gameplay.neural;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dan200.computercraft.ComputerCraft;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.squiddev.plethora.gameplay.ItemBase;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.COMPUTER_ID;

public class CraftingNeuralInterface extends ShapedOreRecipe {
	public CraftingNeuralInterface(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer) {
		super(group, result, primer);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		ItemStack output = getRecipeOutput().copy();

		ItemStack old = inv.getStackInRowAndColumn(1, 1);
		int id = ComputerCraft.Items.pocketComputer.getComputerID(old);
		String label = ComputerCraft.Items.pocketComputer.getLabel(old);

		// Copy across key properties
		CompoundNBT tag = ItemBase.getTag(output);
		if (label != null) output.setStackDisplayName(label);
		if (id >= 0) tag.setInteger(COMPUTER_ID, id);

		// Copy across custom ROM if required
		CompoundNBT fromTag = old.getTagCompound();
		if (fromTag != null && fromTag.hasKey("rom_id")) {
			tag.setTag("rom_id", fromTag.getTag("rom_id"));
		}

		return output;
	}

	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			String group = JsonUtils.getString(json, "group", "");

			Map<Character, Ingredient> ingMap = new HashMap<>();
			for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet()) {
				if (entry.getKey().length() != 1) {
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
				}
				if (" ".equals(entry.getKey())) {
					throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
				}

				ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
			}

			ingMap.put(' ', Ingredient.EMPTY);

			JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

			if (patternJ.size() == 0) {
				throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
			}

			String[] pattern = new String[patternJ.size()];
			for (int x = 0; x < pattern.length; ++x) {
				String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
				if (x > 0 && pattern[0].length() != line.length()) {
					throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
				}
				pattern[x] = line;
			}

			CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
			primer.width = pattern[0].length();
			primer.height = pattern.length;
			primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
			primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);

			Set<Character> keys = new HashSet<>(ingMap.keySet());
			keys.remove(' ');

			int x = 0;
			for (String line : pattern) {
				for (char chr : line.toCharArray()) {
					Ingredient ing = ingMap.get(chr);
					if (ing == null) {
						throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
					}
					primer.input.set(x++, ing);
					keys.remove(chr);
				}
			}

			if (!keys.isEmpty()) {
				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
			}

			ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
			return new CraftingNeuralInterface(group.isEmpty() ? null : new ResourceLocation(group), result, primer);
		}
	}
}
