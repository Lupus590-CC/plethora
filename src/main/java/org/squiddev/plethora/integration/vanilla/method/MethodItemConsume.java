package org.squiddev.plethora.integration.vanilla.method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.method.BasicObjectMethod;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IPartialContext;
import org.squiddev.plethora.api.reference.ItemSlot;

import javax.annotation.Nonnull;

/**
 * A provider for consumables: food and potions.
 *
 * This enables consuming it: Eating/Drinking it
 */
@Injects
public final class MethodItemConsume extends BasicObjectMethod<ItemSlot> {
	public MethodItemConsume() {
		super("consume", true, "function() -- Consume one item from this stack");
	}

	@Override
	public boolean canApply(@Nonnull IPartialContext<ItemSlot> context) {
		if (!context.getTarget().canReplace()) return false;

		ItemStack stack = context.getTarget().getStack();

		if (!stack.isEmpty() && context.hasContext(EntityPlayer.class)) {
			EnumAction action = stack.getItemUseAction();
			return action == EnumAction.EAT || action == EnumAction.DRINK;
		}

		return false;
	}

	@Override
	public Object[] apply(@Nonnull IContext<ItemSlot> context, @Nonnull Object[] args) {
		ItemSlot slot = context.getTarget();
		ItemStack stack = slot.getStack();
		EntityPlayer player = context.getContext(EntityPlayer.class);

		slot.replace(stack.onItemUseFinish(player.getEntityWorld(), player));
		return null;
	}
}
