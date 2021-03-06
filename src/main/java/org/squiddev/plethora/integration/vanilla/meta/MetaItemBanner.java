package org.squiddev.plethora.integration.vanilla.meta;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackMetaProvider;
import org.squiddev.plethora.utils.TypedField;

import javax.annotation.Nonnull;
import java.util.*;

@Injects
public final class MetaItemBanner extends ItemStackMetaProvider<ItemBanner> {
	private static final TypedField<BannerPattern, String> FIELD_NAME = TypedField.of(BannerPattern.class, "fileName", "field_191014_N");

	public MetaItemBanner() {
		super(ItemBanner.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull ItemStack stack, @Nonnull ItemBanner banner) {
		List<Map<String, ?>> out;

		CompoundNBT tag = stack.getSubCompound("BlockEntityTag");
		if (tag != null && tag.hasKey("Patterns")) {
			ListNBT listNBT = tag.getTagList("Patterns", 10);

			out = new ArrayList<>(listNBT.tagCount());
			for (int i = 0; i < listNBT.tagCount() && i < 6; ++i) {
				CompoundNBT patternTag = listNBT.getCompoundTagAt(i);

				EnumDyeColor color = EnumDyeColor.byDyeDamage(patternTag.getInteger("Color"));
				BannerPattern pattern = getPatternByID(patternTag.getString("Pattern"));

				if (pattern != null) {
					Map<String, String> entry = new HashMap<>();
					entry.put("id", pattern.getHashname());
					entry.put("name", FIELD_NAME.get(pattern));

					entry.put("colour", color.toString());
					entry.put("color", color.toString());

					out.add(entry);
				}
			}
		} else {
			out = Collections.emptyList();
		}

		return Collections.singletonMap("banner", out);
	}

	@Nonnull
	@Override
	public ItemStack getExample() {
		ListNBT patterns = new ListNBT();

		CompoundNBT pattern1 = new CompoundNBT();
		pattern1.setString("Pattern", BannerPattern.CREEPER.getHashname());
		pattern1.setInteger("Color", 5);

		patterns.appendTag(pattern1);

		return ItemBanner.makeBanner(EnumDyeColor.GREEN, patterns);
	}

	private static BannerPattern getPatternByID(String id) {
		for (BannerPattern pattern : BannerPattern.values()) {
			if (pattern.getHashname().equals(id)) return pattern;
		}

		return null;
	}
}
