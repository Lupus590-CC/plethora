package org.squiddev.plethora.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TinySlot {
	private final ItemStack stack;

	public TinySlot(@Nonnull ItemStack stack) {
		Objects.requireNonNull(stack, "stack cannot be null");
		this.stack = stack;
	}

	@Nonnull
	public ItemStack getStack() {
		return stack;
	}

	public void markDirty() {
	}

	public static class InventorySlot extends TinySlot {
		private final IInventory inventory;

		public InventorySlot(@Nonnull ItemStack stack, @Nonnull IInventory inventory) {
			super(stack);
			this.inventory = inventory;
		}

		@Override
		public void markDirty() {
			inventory.markDirty();
		}
	}
}
