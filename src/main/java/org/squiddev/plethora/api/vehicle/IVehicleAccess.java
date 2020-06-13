package org.squiddev.plethora.api.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

/**
 * An access object for a computer mounted on some vehicle, usable by {@link IVehicleUpgradeHandler}.
 */
public interface IVehicleAccess {
	/**
	 * Get the vehicle this access object represents.
	 *
	 * @return The access's vehicle.
	 */
	@Nonnull
	Entity getVehicle();

	/**
	 * Get data specific to this stack. This can be written
	 * to and shared with the client. It is not bound to the item
	 * stack however and is discarded when the upgrade is removed.
	 *
	 * If you change this data, you should mark it as dirty with {@link #markDataDirty()}.
	 *
	 * @return The module specific data
	 * @see #markDataDirty()
	 */
	@Nonnull
	CompoundNBT getData();

	/**
	 * Mark the module specific data as dirty
	 *
	 * @see #getData()
	 */
	void markDataDirty();
}
