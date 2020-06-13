package org.squiddev.plethora.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public final class IngredientBrew extends Ingredient {
	private final Effect effect;

	private final ItemStack[] basicStacks;
	private IntList packed;

	private IngredientBrew(Effect effect, Potion potionType) {
		super(Stream.empty());
		this.effect = effect;

		basicStacks = new ItemStack[3];
		basicStacks[0] = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), potionType);
		basicStacks[1] = PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), potionType);
		basicStacks[2] = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType);
	}

	@Override
	@Nonnull
	public ItemStack[] getMatchingStacks() {
		return basicStacks;
	}

	@Override
	@Nonnull
	public IntList getValidItemStacksPacked() {
		if (packed == null) {
			packed = new IntArrayList();
			for (ItemStack stack : basicStacks) packed.add(RecipeItemHelper.pack(stack));
			packed.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return packed;
	}

//	@Override
//	public boolean apply(@Nullable ItemStack target) {
//		if (target == null || target.isEmpty()) return false;
//
//		for (EffectInstance effect : PotionUtils.getEffectsFromStack(target)) {
//			if (effect.getPotion() == this.effect) return true;
//		}
//
//		return false;
//	}

	@Override
	protected void invalidate() {
		packed = null;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

//	public static class Factory implements IIngredientFactory {
//		@Nonnull
//		@Override
//		public Ingredient parse(JsonContext context, JsonObject json) {
//			ResourceLocation effect = new ResourceLocation(JsonUtils.getString(json, "effect"));
//			if (!ForgeRegistries.POTIONS.containsKey(effect)) {
//				throw new JsonSyntaxException("Unknown effect '" + effect + "'");
//			}
//
//			ResourceLocation potion = new ResourceLocation(JsonUtils.getString(json, "potion"));
//			if (!ForgeRegistries.POTION_TYPES.containsKey(potion)) {
//				throw new JsonSyntaxException("Unknown potion '" + potion + "'");
//			}
//
//			return new IngredientBrew(ForgeRegistries.POTIONS.getValue(effect), ForgeRegistries.POTION_TYPES.getValue(potion));
//		}
//	}
}
