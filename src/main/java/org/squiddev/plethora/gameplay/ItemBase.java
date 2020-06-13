package org.squiddev.plethora.gameplay;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.squiddev.plethora.utils.Helpers;

import java.util.List;

public abstract class ItemBase extends Item {

	public ItemBase(String itemName, int stackSize) {
		setTranslationKey(Plethora.RESOURCE_DOMAIN + "." + itemName);
		setRegistryName(new ResourceLocation(Plethora.ID, itemName));

		setCreativeTab(Plethora.getCreativeTab());
		setMaxStackSize(stackSize);
	}

	public ItemBase(String itemName) {
		this(itemName, 64);
	}

	public static CompoundNBT getTag(ItemStack stack) {
		CompoundNBT tag = stack.getTagCompound();
		if (tag == null) stack.setTagCompound(tag = new CompoundNBT());
		return tag;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> out, ITooltipFlag flag) {
		super.addInformation(stack, world, out, flag);
		out.add(Helpers.translateToLocal(getTranslationKey(stack) + ".desc"));
	}
}
