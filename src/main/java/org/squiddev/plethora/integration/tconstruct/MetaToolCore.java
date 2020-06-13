package org.squiddev.plethora.integration.tconstruct;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.ItemStackContextMetaProvider;
import org.squiddev.plethora.api.method.IPartialContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.harvest.TinkerHarvestTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Injects(TConstruct.modID)
public final class MetaToolCore extends ItemStackContextMetaProvider<ToolCore> {
	public MetaToolCore() {
		super("tool", ToolCore.class);
	}

	@Nonnull
	@Override
	public Map<String, ?> getMeta(@Nonnull IPartialContext<ItemStack> context, ToolCore tool) {
		ItemStack stack = context.getTarget();

		Map<String, Object> out = new HashMap<>();

		if (tool.hasCategory(Category.HARVEST)) {
			out.put("miningSpeed", ToolHelper.getActualMiningSpeed(stack));
		}

		out.put("attack", ToolHelper.getActualAttack(stack));
		out.put("freeModifiers", ToolHelper.getFreeModifiers(stack));

		out.put("maxDurability", ToolHelper.getDurabilityStat(stack));
		out.put("durability", ToolHelper.getCurrentDurability(stack));

		{
			// Gather a list of all modifiers. We don't provide what they do, but this should be enough.
			Map<Integer, Map<String, String>> modifiers = new HashMap<>();
			int modIndex = 0;

			ListNBT tagList = TagUtil.getModifiersTagList(stack);
			for (int i = 0; i < tagList.tagCount(); i++) {
				CompoundNBT tag = tagList.getCompoundTagAt(i);
				ModifierNBT data = ModifierNBT.readTag(tag);

				// get matching modifier
				IModifier modifier = TinkerRegistry.getModifier(data.identifier);
				if (modifier == null || modifier.isHidden()) continue;

				Map<String, String> modifierData = new HashMap<>();
				modifierData.put("id", modifier.getIdentifier());
				modifierData.put("name", modifier.getLocalizedName());

				modifiers.put(++modIndex, modifierData);
			}
			out.put("modifiers", modifiers);
		}

		{
			// Gather a list of all parts for this tool
			Map<Integer, Object> parts = new HashMap<>();

			List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack));
			List<PartMaterialType> component = tool.getRequiredComponents();

			if (materials.size() >= component.size()) {
				int partIdx = 0;
				for (int i = 0; i < component.size(); i++) {
					PartMaterialType pmt = component.get(i);
					Material material = materials.get(i);

					// get (one possible) toolpart used to craft the thing
					Iterator<IToolPart> partIter = pmt.getPossibleParts().iterator();
					if (!partIter.hasNext()) continue;

					IToolPart part = partIter.next();
					ItemStack partStack = part.getItemstackWithMaterial(material);
					if (partStack != null) {
						parts.put(++partIdx, context.makePartialChild(pmt).makePartialChild(partStack).getMeta());
					}
				}
			}

			out.put("parts", parts);
		}


		return out;
	}

	@Nullable
	@Override
	public ItemStack getExample() {
		ToolCore tool = TinkerHarvestTools.pickaxe;
		int required = tool.getRequiredComponents().size();
		List<Material> mats = new ArrayList<>(required);

		Collection<Material> materials = TinkerRegistry.getAllMaterials();
		for (Material material : materials) {
			mats.clear();
			for (int i = 0; i < required; i++) mats.add(material);

			ItemStack stack = tool.buildItem(mats);
			if (tool.hasValidMaterials(stack)) return stack;
		}

		return null;
	}
}
